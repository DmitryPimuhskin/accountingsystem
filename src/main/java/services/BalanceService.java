package services;

import databaselogic.controllers.DBBalanceController;
import domain.Balance;
import domain.RefreshBalanceData;
import entities.AccoutingHistory;
import entities.Detail;
import entities.PrimitivityBalance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ChainUtil;
import utils.Searcher;
import utils.enums.RussianMonths;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BalanceService {
    private static final Logger logger = LogManager.getLogger(BalanceService.class);
    private static final DBBalanceController controller = new DBBalanceController();

    public static List<Balance> initializingDataInTable() {

        List<Balance> initialBalances = BalanceService.buildBalances();
        if (initialBalances != null)
            return initialBalances;
        return FXCollections.observableArrayList();
    }

    public static List<PrimitivityBalance> buildPrimitivs(Detail detail) {
        List<PrimitivityBalance> pBalances;
        int idDetail = detail.getId();
        PrimitivityBalance p1 = new PrimitivityBalance(idDetail, Year.now(), Month.JANUARY);
        PrimitivityBalance p2 = new PrimitivityBalance(idDetail, Year.now(), Month.FEBRUARY);
        PrimitivityBalance p3 = new PrimitivityBalance(idDetail, Year.now(), Month.MARCH);
        PrimitivityBalance p4 = new PrimitivityBalance(idDetail, Year.now(), Month.APRIL);
        PrimitivityBalance p5 = new PrimitivityBalance(idDetail, Year.now(), Month.MAY);
        PrimitivityBalance p6 = new PrimitivityBalance(idDetail, Year.now(), Month.JUNE);
        PrimitivityBalance p7 = new PrimitivityBalance(idDetail, Year.now(), Month.JULY);
        PrimitivityBalance p8 = new PrimitivityBalance(idDetail, Year.now(), Month.AUGUST);
        PrimitivityBalance p9 = new PrimitivityBalance(idDetail, Year.now(), Month.SEPTEMBER);
        PrimitivityBalance p10 = new PrimitivityBalance(idDetail, Year.now(), Month.OCTOBER);
        PrimitivityBalance p11 = new PrimitivityBalance(idDetail, Year.now(), Month.NOVEMBER);
        PrimitivityBalance p12 = new PrimitivityBalance(idDetail, Year.now(), Month.DECEMBER);

        pBalances = Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12);
        return pBalances;
    }

    public static List<Balance> buildBalances() {
        List<PrimitivityBalance> pBalances = controller.getAll();
        if (pBalances == null)
            return null;
        List<AccoutingHistory> histories = AccoutingHistoryService.getAll();
        List<Detail> details = DetailService.getAll();
        return ChainUtil.createBalanceChain(details, pBalances, histories); // хзззззззззззззззззззхзззззхзхзхзхззззззззззз сложнаааа, ебаная структура бд поэтому каждый раз создавать баланс из такого объема данныъ

    }

    public static void updateBalance(Balance balance) {
        List<PrimitivityBalance> pBalances = decompositionBalance(balance);
        controller.updateAll(pBalances);
    }

    private static List<PrimitivityBalance> decompositionBalance(Balance balance) {
        List<PrimitivityBalance> pBalances = new ArrayList<>();
        for (Map.Entry<Month, Double> month : balance.getOutcoming().entrySet()) {
            pBalances.add(
                    new PrimitivityBalance(
                            balance.getDetail().getId(),
                            balance.getYear(),
                            month.getKey(),
                            balance.getIncoming().get(month.getKey()),
                            month.getValue(),
                            balance.getBalanceAtBeginningYear(),
                            balance.getBalanceAtEndOfYear()
                    )
            );
        }
        return pBalances;
    }

    private static void recalculateTotal(Balance balance) {
        double incSum = 0.0, outSum = 0.0;
        for (Map.Entry<Month, Double> i : balance.getIncoming().entrySet()) {
            incSum += i.getValue();
        }
        for (Map.Entry<Month, Double> i : balance.getOutcoming().entrySet()) {
            outSum += i.getValue();
        }
        double endYear = new BigDecimal(incSum - outSum).setScale(2, RoundingMode.UP).doubleValue();

        balance.setInTotal(incSum);
        balance.setOutTotal(outSum);
        balance.setBalanceAtEndOfYear(endYear);

    }

    // receipt;  приход
    // consumption; расход
    public static void updAccHistoryByDays(Balance balance, Map<RussianMonths, List<AccoutingHistory>> candidates) {
        double[] sums;
        for (Map.Entry<RussianMonths, List<AccoutingHistory>> candidate : candidates.entrySet()) {
            Month key = Searcher.searchEngMonthByRus(candidate.getKey());
            sums = AccoutingHistoryService.calculate(candidate.getValue());
            balance.updateIncomingValue(key, sums[0]);
            balance.updateOutcomingValue(key, sums[1]);
        }
        recalculateTotal(balance);
        updateBalance(balance);

    }
// TODO: some optimization?
    public static List<Balance> updBalanceWhenProduceRawElectrode(List<RefreshBalanceData> updData, ObservableList<Balance> balances){
        List<Balance> updBalances = new ArrayList<>();
        updData.forEach(data ->{
            Balance balance = balances.stream().filter(b -> b.getDetail().getId()==data.idDetail).findFirst().get();
            Month key = data.month;
            double oldValue = balance.getOutcoming().get(key);
            balance.getOutcoming().replace(key, oldValue, oldValue + data.value);
            recalculateTotal(balance);
            updateBalance(balance);
            updBalances.add(balance);
        });

        return updBalances;
    }
}
