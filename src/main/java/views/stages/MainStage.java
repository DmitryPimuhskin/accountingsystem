package views.stages;

import databaselogic.controllers.DBAccountingHistoryController;
import databaselogic.controllers.DBBalanceController;
import databaselogic.controllers.DBDetailController;
import domain.Balance;
import entities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import projectConstants.CustomConstants;
import services.*;
import utils.ChainUtil;
import utils.documentGeneration.DocumentService;
import utils.enums.RussianMonths;
import utils.enums.Types;
import views.alerts.Alerts;
import views.dropBoxes.DetailDropBox;
import views.modalWindows.AccoutingHistoryWindow;
import views.tables.*;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class MainStage {
    private static final Logger logger = LogManager.getLogger(MainStage.class);

    private static final Stage stage = new Stage();
    private Scene scene;
    //layouts
    private BorderPane layout;
    private final BorderPane paneForBalanceTab = new BorderPane();
    private final BorderPane paneForAccoutingESMGTab = new BorderPane();
    private final BorderPane paneForAccoutingESMGMTab = new BorderPane();
    private final BorderPane paneForCostDetail = new BorderPane();
    private TabPane tabPane;
    //tables
    private final BalancesTable balancesTable = new BalancesTable();
    private final SummaryTable summaryTable = new SummaryTable();
    private final CostDetailTable costDetailTable = new CostDetailTable();
    private final ComponentsConsumptionESMGTable esmgTable = new ComponentsConsumptionESMGTable();
    private final ComponentsConsumptionESMGMTable esmgmTable = new ComponentsConsumptionESMGMTable();
    private final RawElectrodeTable rawTable = new RawElectrodeTable();

    //custom classes
    private DetailDropBox balanceDetailDropBox;
    private DetailDropBox ddb;
    private DetailDropBox ddbm;

    private DBAccountingHistoryController ahController = new DBAccountingHistoryController();
    private DBDetailController detailController = new DBDetailController();
    private DBBalanceController balanceController = new DBBalanceController();

    private DocumentService documentService;

    public MainStage() {
        init();
    }

    private void init() {
        initTabs();
        initLayout();
        initScene();
        initStage();

    }

    private void initStage() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = dim.width;
        int maxHeight = dim.height;
        stage.setResizable(true);
        stage.setMaxHeight(maxHeight-50);
        stage.setMaxWidth(maxWidth-5);
        stage.setMinHeight(maxHeight%2);
        stage.setMinWidth(maxWidth%2);
        stage.setTitle("Система учета электродов");
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void initScene() {
        scene = new Scene(layout);
    }

    private void initLayout() {
        layout = new BorderPane();
        layout.setCenter(tabPane);
    }

    private void initTabs() {
        tabPane = new TabPane();

        Tab accounting = new Tab("Остатки");
        accounting.setClosable(false);
        Tab costDetailTab = new Tab("Детали");
        costDetailTab.setClosable(false);
        Tab componentsConsumptionForESMG = new Tab("Расход комплектующих для ЭСМГ");
        componentsConsumptionForESMG.setClosable(false);
        Tab componentsConsumptionForESMGM = new Tab("Расход комплектующих для ЭСМГ-М");
        componentsConsumptionForESMGM.setClosable(false);
        Tab electrods = new Tab("Электроды");
        electrods.setClosable(false);

        componentsConsumptionForESMGM.setContent(esmgmTable.getTable());

        addLogicOnSummaryTab(electrods);
        addLogicOnAccoutingESMG_MTab(componentsConsumptionForESMGM);
        addLogicOnAccoutingESMGTab(componentsConsumptionForESMG);
        addLogicOnBalanceTab(accounting);
        addLogicOnCostDetailTab(costDetailTab);

        tabPane.getTabs().addAll(
                accounting,
                costDetailTab,
                componentsConsumptionForESMG,
                componentsConsumptionForESMGM,
                electrods
        );
    }
//[BalanceLogic]
    private void addLogicOnBalanceTab(Tab tab) {
        balancesTable.initBalances(BalanceService.initializingDataInTable());
        tab.setContent(paneForBalanceTab);
        paneForBalanceTab.setCenter(balancesTable.getTable());

        HBox horizontal = new HBox(10);
        horizontal.setPadding(new Insets(10, 0, 10, 0));
        horizontal.setAlignment(Pos.BOTTOM_CENTER);

        balanceDetailDropBox = new DetailDropBox();
        if (balancesTable.getDetails()!=null)
            balancesTable.getDetails().forEach(balanceDetailDropBox::deleteDetail);

        Button history = new Button("История");
        Button add = new Button("Добавить");

        add.setOnAction(event -> {
            logger.info("[BalanceLogic.ADD] START adding balance in table...");
            //initRawElectrodeValue detail from drop box
            Detail detail = balanceDetailDropBox.getDetailsBox().getSelectionModel().getSelectedItem();
            balanceDetailDropBox.getDetailsBox().getSelectionModel().clearSelection(); //?
            if (detail == null){
                logger.error("  detail is null");
                Alerts.WARNING_ALERT("Выберите деталь для добавления баланса.");
                return;
            }
            logger.info("   delete detail from dropbox");
            balanceDetailDropBox.deleteDetail(detail);
            logger.info("   build accounting history for detail");
            AccoutingHistoryService.buildSqlForBatchInsertAccHist(detail);
            List<AccoutingHistory> histories = AccoutingHistoryService.getHistoryByDetail(detail);
            logger.info("   build primitives for balance");
            List<PrimitivityBalance> pBalances = BalanceService.buildPrimitivs(detail);
            logger.info("   save primitive balances on database");
            logger.debug("  primitive balances: {} for detail: {}",pBalances,detail);
            balanceController.saveAll(pBalances);
            // initRawElectrodeValue primitive balance from table (with id)
            logger.info("   get primitive balances from database for detail by id ");
            pBalances = balanceController.getAllByDetailId(detail.getId());
            logger.info("   create chain between detail, primitive balances, accounting history");
            logger.debug("  detail: {} ||| primitive balances: {} ||| accounting history: {}",detail,pBalances,histories);
            // hz, research, think can speed up?????
            List<Balance> balances = ChainUtil.createBalanceChain(
                    Collections.singletonList(detail),
                    pBalances,
                    histories
            );
            logger.debug("  chain creation result balances size {}",balances.size());
            if (balances != null && !balances.isEmpty()) {
                logger.info("   added balance in table");
                balancesTable.addBalance(balances.get(0));
            }
            logger.info("[BalanceLogic.ADD] END adding balance in table...");

        });

        history.setOnAction(event -> {
            logger.info("[BalanceLogic.HISTORY] START viewing accounting history...");
            //initRawElectrodeValue detail by selected balance
            Balance balance = balancesTable.getTable().getSelectionModel().getSelectedItem();
            if (balance == null){
                logger.error("  balance not found");
                Alerts.WARNING_ALERT("Выберите баланс для просмотра его истории.");
                return;
            }
            int position = balancesTable.getTable().getItems().indexOf(balance);
            Detail detail = balance.getDetail();
            //initRawElectrodeValue gistory for current detail
            List<AccoutingHistory> ahList = ahController.getByDetail(detail.getId());
            //associated detail with her history
//            ChainUtil.associateDetailWithHistory(detail, ahList);   ?????????
            logger.info("   converting history to map for AccountingWindow [month,history]");
            Map<RussianMonths, List<AccoutingHistory>> tmp = AccoutingHistoryService.historyToMapForAccoutingWindow(ahList);
            logger.info("   sending history map in AccountingWindow and wait wile returning result for update");// (candidates on update)
            tmp = new AccoutingHistoryWindow(tmp).show();
            // send candidates for update into updating logic
            if (tmp != null) {
                logger.info("   updating history for balance");
                BalanceService.updAccHistoryByDays(balance, tmp);
                logger.info("   rewriting balance on table");
                balancesTable.getTable().getItems().set(position, balance);
                logger.info("   updating history in database");
                AccoutingHistoryService.buildSqlForBatchUpdAccHist(tmp);
            }
            logger.info("[BalanceLogic.HISTORY] END viewing accounting history...");
        });

        horizontal.getChildren().addAll(history, balanceDetailDropBox.getDetailsBox(), add);
        paneForBalanceTab.setBottom(horizontal);

    }

    private void addLogicOnCostDetailTab(Tab tab) {
        tab.setContent(paneForCostDetail);
        paneForCostDetail.setCenter(costDetailTable.getCostDetailTable());
        List<DetailDropBox> boxes = Arrays.asList(balanceDetailDropBox,ddb,ddbm);

        TextField title = new TextField();
        title.setPromptText("Введите название детали");
        TextField count = new TextField();
        count.setPromptText("Введите количество детали");
        TextField cost = new TextField();
        cost.setPromptText("Введите стоимость детали");
        TextField descriptions = new TextField();
        descriptions.setPromptText("Примечание");

        Button add = new Button("Добавить");
        Button delete = new Button("Удалить");
        Button commit = new Button("Обновить данные");

        HBox horizontal = new HBox(15);
        horizontal.setPadding(new Insets(10, 0, 10, 0));
        horizontal.setAlignment(Pos.BOTTOM_CENTER);
        horizontal.getChildren().addAll(title, count, cost, descriptions, add, delete,commit);

        paneForCostDetail.setBottom(horizontal);
        commit.setOnAction(event -> costDetailTable.dataUpdate());
        add.setOnAction(event -> {
            String dTitle = title.getText().trim();
            String dCount = count.getText().trim();
            String dCost = cost.getText().trim();
            if (dCost.isEmpty() || dCost==null)
                dCost="0";
            if (dTitle.isEmpty() || dCount.isEmpty()) {
                Alerts.WARNING_ALERT("Вы не заполнили одно из обязательных полей.");
                return;
            }
            Detail d = new Detail(
                    dTitle,
                    Double.valueOf(dCount),
                    new BigDecimal(dCost),
                    descriptions.getText()
            );
            detailController.save(d);
            d = detailController.get(d.getTitle());
            costDetailTable.addDetail(d);
            for (DetailDropBox box: boxes)
                box.addDetail(d);
            title.clear();
            count.clear();
            cost.clear();
            descriptions.clear();

        });
        delete.setOnAction(event -> {
            if (costDetailTable.getCostDetailTable().getSelectionModel().getSelectedItem() == null) {
                Alerts.WARNING_ALERT("Выберите деталь для удаления.");
                return;
            }
            Detail d = costDetailTable.getCostDetailTable().getSelectionModel().getSelectedItem();
            costDetailTable.removeDetail(d);
            detailController.delete(d.getId());
            for (DetailDropBox box: boxes)
                box.deleteDetail(d);
        });
    }

    // neede optimization and changing logic
    private void addLogicOnAccoutingESMGTab(Tab tab) {
        tab.setContent(paneForAccoutingESMGTab);
        paneForAccoutingESMGTab.setCenter(esmgTable.getTable());
        HBox horizontal = new HBox(10);

        ddb = new DetailDropBox();
        TextField count = new TextField();
        count.setPromptText("количество деталей");
        TextField cost = new TextField();
        cost.setPromptText("стоимость детали");
        Button add = new Button("Добавить");
        Button delete = new Button("Удалить");
        Button commit = new Button("Обновить данные");

        horizontal.setPadding(new Insets(10, 0, 10, 0));
        horizontal.setAlignment(Pos.BOTTOM_CENTER);

        horizontal.getChildren().addAll(ddb.getDetailsBox(), count, cost, add, delete,commit);
        paneForAccoutingESMGTab.setBottom(horizontal);

        List<Detail> initDet = esmgTable.getTable().getItems();
        initDet.forEach(ddb::deleteDetail);

        commit.setOnAction(event -> esmgTable.dataUpdate());
        add.setOnAction(event -> {
            Detail detail = ddb.getDetailsBox().getSelectionModel().getSelectedItem();
            Double newCount = Double.valueOf(count.getText().trim()) ;
            BigDecimal newCost = new BigDecimal(cost.getText().trim());
            if (detail == null)
                return;
            DetailElectrodePrimitive primitive = DetailElectrodeService.add(detail,newCount,newCost,Types.ESMG.eng());
            Map<Double, BigDecimal> tmp = new HashMap<>();
            tmp.put(newCount,newCost);
            esmgTable.getDetailElectrods().getDetails().put(detail, tmp);
            esmgTable.getDetailElectrods().getIds().add(primitive.getId());
            esmgTable.getTable().getItems().add(detail);
            ddb.deleteDetail(detail);
            count.clear();
            cost.clear();
        });

        delete.setOnAction(event -> {
            Detail detail = esmgTable.getTable().getSelectionModel().getSelectedItem();
            if (detail == null)
                return;
            esmgTable.getTable().getItems().remove(detail);
            esmgTable.getDetailElectrods().getDetails().remove(detail);
            ddb.addDetail(detail);
            DetailElectrodeService.deleteByDetailAndElType(detail.getId(), Types.ESMG.eng());

        });
    }

    private void addLogicOnAccoutingESMG_MTab(Tab tab) {
        tab.setContent(paneForAccoutingESMGMTab);
        paneForAccoutingESMGMTab.setCenter(esmgmTable.getTable());
        HBox horizontal = new HBox(10);

        ddbm = new DetailDropBox();
        TextField count = new TextField();
        count.setPromptText("количество деталей");
        TextField cost = new TextField();
        cost.setPromptText("стоимость детали");

        Button add = new Button("Добавить");
        Button delete = new Button("Удалить");
        Button commit = new Button("Обновить данные");

        horizontal.setPadding(new Insets(10, 0, 10, 0));
        horizontal.setAlignment(Pos.BOTTOM_CENTER);

        horizontal.getChildren().addAll(ddbm.getDetailsBox(), count, cost, add, delete,commit);
        paneForAccoutingESMGMTab.setBottom(horizontal);
        List<Detail> initDet = esmgmTable.getTable().getItems();
        initDet.forEach(ddbm::deleteDetail);

        commit.setOnAction(event -> esmgmTable.dataUpdate());
        add.setOnAction(event -> {
            Detail detail = ddbm.getDetailsBox().getSelectionModel().getSelectedItem();
            Double newCount = Double.valueOf(count.getText().trim()) ;
            BigDecimal newCost = new BigDecimal(cost.getText().trim());
            if (detail == null)
                return;
            DetailElectrodePrimitive primitive = DetailElectrodeService.add(detail,newCount,newCost,Types.ESMG_M.eng());
            Map<Double, BigDecimal> tmp = new HashMap<>();
            tmp.put(newCount,newCost);
            esmgmTable.getDetailElectrods().getDetails().put(detail, tmp);
            esmgmTable.getDetailElectrods().getIds().add(primitive.getId());
            esmgmTable.getTable().getItems().add(detail);
            ddbm.deleteDetail(detail);
            count.clear();
            cost.clear();

        });

        delete.setOnAction(event -> {
            Detail detail = esmgmTable.getTable().getSelectionModel().getSelectedItem();
            if (detail == null)
                return;
            esmgmTable.getTable().getItems().remove(detail);
            esmgmTable.getDetailElectrods().getDetails().remove(detail);
            ddbm.addDetail(detail);
            DetailElectrodeService.deleteByDetailAndElType(detail.getId(), Types.ESMG_M.eng());

        });
    }

    private void addLogicOnSummaryTab(Tab tab) {
        BorderPane pane = new BorderPane();
        BorderPane paneForGridAndRawTable = new BorderPane();
        tab.setContent(pane);

        pane.setPadding(new Insets(10));
        pane.setCenter(summaryTable.getTable());

        Label rawTitle = new Label("Производство сырых электродов");
        Label elecTitle = new Label("Продажа электродов");
        Label docTitle = new Label("Формирование документа");

        Label typeL = new Label("Тип электрода");
        ComboBox<String> types = new ComboBox<>();
        types.getItems().addAll(Types.ESMG.eng(), Types.ESMG_M.eng());

        Label produceDateL = new Label("Дата производства");
        DatePicker produceDate = new DatePicker();
        produceDate.setValue(LocalDate.now());
        produceDate.setShowWeekNumbers(true);

        Label customerL = new Label("Заказчик");
        TextField customer = new TextField();

        Label consumeDateL = new Label("Дата отгрузки");
        DatePicker consumeDate = new DatePicker();
        consumeDate.setValue(LocalDate.now());
        consumeDate.setShowWeekNumbers(true);

        Label noteL = new Label("Примечание");
        TextField note = new TextField();

        TextField nFrom = new TextField();
        nFrom.setPromptText("с № электрода");
        TextField nTo = new TextField();
        nTo.setPromptText("по № электрода");

        TextField rawProduction = new TextField();
        rawProduction.setPromptText("кол-во сырьевых электродов");

        Label positionL = new Label("Должность");
        TextField position = new TextField();
        position.setPromptText("должность");
        Label fioL = new Label("ФИО");
        TextField fio = new TextField("");
        fio.setPromptText("ФИО через пробел");
        Label cableLengthL = new Label("Длина кабеля");
        TextField cableLength = new TextField("");
        cableLength.setPromptText("длина кабеля");
        Label docDateL = new Label("Дата");
        DatePicker docDate = new DatePicker();
        docDate.setValue(LocalDate.now());
        docDate.setShowWeekNumbers(true);
        setDateFormat(Arrays.asList(produceDate, consumeDate,docDate));

        Button delete = new Button("Удалить");
        Button bulkProduce = new Button("Произвести электрод");
        Button rawProduce = new Button("Сырьевой электрод");

        rawProduce.setOnAction(event -> {
            String count = rawProduction.getText().trim();
            String type = types.getSelectionModel().getSelectedItem();

            if (count.isEmpty() || type.isEmpty()){
                Alerts.WARNING_ALERT("Не заполнено обязательное поле или не выбран тип электрода.");
                return;}
            // хуевая идея передавать список балансов в метод, переделать блять (или нет)
            ObservableList<Balance> updBalance = FXCollections.observableList(
                    CountingService.countingForProduceRawElectrode(type, Integer.valueOf(count), balancesTable.getBalances())
            );
            rawTable.refresh();
            if (!updBalance.isEmpty()) {
                costDetailTable.refresh();
                balancesTable.ref(updBalance);
            }
            rawProduction.clear();
        });

        try {
            documentService = new DocumentService();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bulkProduce.setOnAction(event -> {
            String from = nFrom.getText().trim();
            String to = nTo.getText().trim();
            String type = types.getSelectionModel().getSelectedItem();
            String empPosition = position.getText().trim();
            String cabLen = cableLength.getText().trim();
            String empFio = fio.getText().trim();
            String doc = docDate.getValue().toString();

            if ((type.isEmpty() && type!=null)||(from.isEmpty() && to.isEmpty())){
                Alerts.WARNING_ALERT("Выберите тип электрода и введите количество");
                return;
            }

            boolean isDuplicate = SummaryService.checkOnDuplicateNumbers(from,summaryTable.getSummaries());
            if (isDuplicate){
                Alerts.WARNING_ALERT("Электрод с таким номером уже существует");
                return;
            }

            if (to.isEmpty() || to==null){
                CountingService.countingForProduceSummaryFromRawElectrode("0", "1", type);
                Summary summary = new Summary(from, type, produceDate.getValue(), customer.getText().trim(), consumeDate.getValue(), note.getText().trim());
                SummaryService.save(summary);
                if (!empPosition.isEmpty() && !cabLen.isEmpty() && !empFio.isEmpty() && !doc.isEmpty()) {
                    documentService.generateDocumentByType(type,from,"",cabLen,empPosition,empFio,doc);
                }
            } else {
                CountingService.countingForProduceSummaryFromRawElectrode(from, to, type);
                SummaryService.bulkCreateSummaryFromRange(from, to, type, produceDate.getValue(), consumeDate.getValue(), customer.getText(), note.getText());
                if (!empPosition.isEmpty() && !cabLen.isEmpty() && !empFio.isEmpty() && !doc.isEmpty()) {
                    documentService.generateDocumentByType(type,from,to,cabLen,empPosition,empFio,doc);
                }

            }

            nFrom.clear();
            nTo.clear();
            customer.clear();
            note.clear();
            position.clear();
            cableLength.clear();
            fio.clear();

            summaryTable.refresh();
            rawTable.refresh();
        });

        delete.setOnAction(event -> {
            Summary summary = summaryTable.getTable().getSelectionModel().getSelectedItem();
            if (summary == null){
                Alerts.WARNING_ALERT("Выберите элемент для удаления.");
                return; } // TODO ERROR: добавить обработку ошибки (всплывающее сообщение)
            summaryTable.deleteSummary(summary);
            SummaryService.delete(summary);

        });

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(12);
        gridPane.setPadding(new Insets(10));
        //raw el
        gridPane.add(rawTitle, 0, 0);
        gridPane.add(typeL, 0, 2);
        gridPane.add(types, 1, 2);
        gridPane.add(rawProduction,0,3);
        gridPane.add(rawProduce, 1, 3);
        //el
        gridPane.add(elecTitle, 0, 4);
        gridPane.add(nFrom, 0, 5);
        gridPane.add(nTo, 1, 5);
        gridPane.add(produceDateL, 0, 6);
        gridPane.add(produceDate, 1, 6);
        gridPane.add(customerL, 0, 7);
        gridPane.add(customer, 1, 7);
        gridPane.add(consumeDateL, 0, 8);
        gridPane.add(consumeDate, 1, 8);
        gridPane.add(noteL, 0, 9);
        gridPane.add(note, 1, 9);
        //doc
        gridPane.add(docTitle,0,11);
        gridPane.add(cableLengthL,0,12);
        gridPane.add(cableLength,1,12);
        gridPane.add(positionL,0,13);
        gridPane.add(position,1,13);
        gridPane.add(fioL,0,14);
        gridPane.add(fio,1,14);
        gridPane.add(docDateL,0,15);
        gridPane.add(docDate,1,15);
        gridPane.add(bulkProduce, 1, 16);
        gridPane.add(delete,0,16);
        pane.setRight(paneForGridAndRawTable);
        paneForGridAndRawTable.setTop(gridPane);
        paneForGridAndRawTable.setCenter(rawTable.getTable());

    }

    private void setDateFormat(List<DatePicker> datePickers) {
        datePickers.forEach(datePicker -> {
            datePicker.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate object) {
                    if (object != null)
                        return CustomConstants.DATE_TIME_FORMATTER.format(object);
                    else return null;
                }

                @Override
                public LocalDate fromString(String string) {
                    if (string != null && !string.isEmpty()) {
                        return LocalDate.parse(string, CustomConstants.DATE_TIME_FORMATTER);
                    } else {
                        return null;
                    }
                }
            });
        });
    }

    public Stage getStage() {
        return stage;
    }
}
