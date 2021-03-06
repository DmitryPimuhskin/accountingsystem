package utils.documentGeneration;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import services.ElectrodeService;
import utils.enums.Paths;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DocESMGM_V2 {
    public void theDocCont(
            String elNumberFrom, String elNumberTo,
            String cableLength, String empoyerPosition,
            String fullName, String createDate, File qr) throws IOException, InvalidFormatException {

        XWPFDocument documentCont = new XWPFDocument(); // делаем наш документ

        XWPFParagraph paragraph = documentCont.createParagraph(); // делаем первый параграф нашего дока
        XWPFRun run = paragraph.createRun();

        run.setText("11. Свидетельство о приемке");

        run.setFontSize(14);
        run.setFontFamily("Times New Roman");
        run.setBold(true);
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        XWPFParagraph paragraph2 = documentCont.createParagraph();
        XWPFRun run2 = paragraph2.createRun();
        run2.setText("Электрод сравнения неполяризующийся медно-сульфатный гелевый");
        run2.setFontSize(13);
        run2.setFontFamily("Times New Roman");
        run2.setBold(true);
        run2.setUnderline(UnderlinePatterns.SINGLE);
        run2.addBreak();

        XWPFRun run3 = paragraph2.createRun();
        run3.setText("наименование изделия");
        run3.setFontSize(11);
        run3.setFontFamily("Times New Roman");
        run3.addBreak();

        XWPFParagraph paragraph3 = documentCont.createParagraph();

        XWPFRun run4 = paragraph3.createRun();
        run4.setText("ЭСМГ-М-АКТИВ");
        run4.setFontSize(13);
        run4.setFontFamily("Times New Roman");
        run4.setBold(true);
        run4.setUnderline(UnderlinePatterns.SINGLE);
        run4.addBreak();

        XWPFRun run5 = paragraph3.createRun();
        run5.setText("   обозначение");
        run5.addBreak();
        run5.setText("");
        run5.setFontSize(11);
        run5.setFontFamily("Times New Roman");
        run5.addBreak();

        XWPFRun run6 = paragraph3.createRun();
        run6.setText("ТУ 28.99.39 – 005 – 37064207 – 2017");
        run6.setFontSize(13);
        run6.setFontFamily("Times New Roman");
        run6.setBold(true);
        run6.setUnderline(UnderlinePatterns.SINGLE);
        run6.addBreak();

        XWPFParagraph paragraph4 = documentCont.createParagraph();

        XWPFRun run7 = paragraph4.createRun();
        int elCount = 1;
        elNumberFrom = ElectrodeService.formatElectrodeNumber(elNumberFrom);
        if (elNumberTo.isEmpty()) {
            run7.setText("№" + " " + elNumberFrom);
        } else {
            elCount =Integer.valueOf(elNumberTo) - Integer.valueOf(elNumberFrom);
            elNumberTo = ElectrodeService.formatElectrodeNumber(elNumberTo);
            run7.setText("№ " + elNumberFrom + " - " + elNumberTo);
        }
        run7.setFontSize(12);
        run7.setFontFamily("Times New Roman");
        run7.setUnderline(UnderlinePatterns.SINGLE);
        run7.addBreak();

        XWPFRun run8 = paragraph4.createRun();
        run8.setText("заводской номер");
        run8.setFontSize(11);
        run8.setFontFamily("Times New Roman");

//       Количество электродов: _______ шт.
        XWPFParagraph paragraph9 = documentCont.createParagraph();

        XWPFRun run23 = paragraph9.createRun();
        run23.setText(String.format("    Количество электродов: %d шт.",elCount));
        run23.setFontSize(12);
        run23.setFontFamily("Times New Roman");
        run23.addBreak();
//----

        XWPFParagraph paragraph5 = documentCont.createParagraph();

        XWPFRun run9 = paragraph5.createRun();
        run9.setText("    Длина кабеля:" + " " + cableLength + " " + "м.");
        run9.setFontSize(12);
        run9.setFontFamily("Times New Roman");
        run9.addBreak();

//        XWPFRun run10 = paragraph5.createRun();
//        run10.setText("     Упакован на");
//        run10.setFontSize(13);
//        run10.setFontFamily("Times New Roman");

        XWPFParagraph paragraph6 = documentCont.createParagraph();

//        XWPFRun run11 = paragraph6.createRun();
//        run11.setText("ООО НПО \"Активация\"");
//        run11.setFontSize(14);
//        run11.setFontFamily("Times New Roman");
//        run11.setBold(true);
//        run11.setUnderline(UnderlinePatterns.SINGLE);
//        run11.addBreak();
//
//        XWPFRun run12 = paragraph6.createRun();
//        run12.setText("наименование или код изготовителя");
//        run12.setFontSize(11);
//        run12.setFontFamily("Times New Roman");
//        run12.addBreak();

        XWPFRun run13 = paragraph6.createRun();
        run13.setText("изготовлен и принят в соответствии с обязательными требованиями государственных стандартов, действующей технической документацией и признан годным для эксплуатации");
        run13.setFontSize(12);
        run13.setFontFamily("Times New Roman");
        run13.addBreak();

        XWPFParagraph paragraph7 = documentCont.createParagraph();

        XWPFRun run14 = paragraph7.createRun();
        run14.setText(" " + empoyerPosition);
        run14.setFontSize(13);
        run14.setFontFamily("Times New Roman");
        run14.setItalic(true);
        run14.setUnderline(UnderlinePatterns.SINGLE);
        run14.addBreak();

        XWPFRun run15 = paragraph7.createRun();
        run15.setText(" " + "должность");
        run15.addBreak();
        run15.setText(" ");
        run15.setFontSize(11);
        run15.setFontFamily("Times New Roman");
        run15.addBreak();

        XWPFRun run16 = paragraph7.createRun();
        run16.setText(" " + "_____________");
        run16.setFontSize(13);
        run16.setFontFamily("Times New Roman");
        run16.addBreak();

        XWPFRun run17 = paragraph7.createRun();
        run17.setText(" " + "личная подпись");
        run17.addBreak();
        run17.setText(" ");
        run17.setFontSize(11);
        run17.setFontFamily("Times New Roman");
        run17.addBreak();

        XWPFParagraph paragraph8 = documentCont.createParagraph();
        XWPFRun run18 = paragraph8.createRun();
        String imgFile = "gQR2.jpg";
        try(FileInputStream is = new FileInputStream(qr)) {
            run18.addBreak();
            run18.addPicture(is, XWPFDocument.PICTURE_TYPE_JPEG, imgFile, Units.toEMU(100), Units.toEMU(100)); // 200x200 pixels
            run18.addBreak();
        }

        XWPFRun run19 = paragraph7.createRun();
        run19.setText(" " + fullName);
        run19.setFontSize(13);
        run19.setFontFamily("Times New Roman");
        run19.setItalic(true);
        run19.setUnderline(UnderlinePatterns.SINGLE);
        run19.addBreak();

        XWPFRun run20 = paragraph7.createRun();
        run20.setText(" " + "расшифровка подписи");
        run20.addBreak();
        run20.setText(" ");
        run20.setFontSize(11);
        run20.setFontFamily("Times New Roman");
        run20.addBreak();

        XWPFRun run21 = paragraph7.createRun();
        run21.setText(" " + createDate);
        run21.setFontSize(13);
        run21.setFontFamily("Times New Roman");
        run21.setItalic(true);
        run21.setUnderline(UnderlinePatterns.SINGLE);
        run21.addBreak();

        XWPFRun run22 = paragraph7.createRun();
        run22.setText(" " + "число, месяц, год");
        run22.setFontSize(11);
        run22.setFontFamily("Times New Roman");

        paragraph2.setAlignment(ParagraphAlignment.CENTER);
        paragraph2.setBorderBottom(Borders.BASIC_THIN_LINES);
        paragraph2.setBorderLeft(Borders.BASIC_THIN_LINES);
        paragraph2.setBorderRight(Borders.BASIC_THIN_LINES);
        paragraph2.setBorderTop(Borders.BASIC_THIN_LINES);

        paragraph3.setAlignment(ParagraphAlignment.LEFT);
        paragraph3.setBorderBottom(Borders.BASIC_THIN_LINES);
        paragraph3.setBorderLeft(Borders.BASIC_THIN_LINES);
        paragraph3.setBorderRight(Borders.BASIC_THIN_LINES);
        paragraph3.setBorderTop(Borders.BASIC_THIN_LINES);

        paragraph4.setAlignment(ParagraphAlignment.CENTER);
        paragraph4.setBorderBottom(Borders.BASIC_THIN_LINES);
        paragraph4.setBorderLeft(Borders.BASIC_THIN_LINES);
        paragraph4.setBorderRight(Borders.BASIC_THIN_LINES);
        paragraph4.setBorderTop(Borders.BASIC_THIN_LINES);

        paragraph5.setAlignment(ParagraphAlignment.LEFT);
        paragraph5.setBorderBottom(Borders.BASIC_THIN_LINES);
        paragraph5.setBorderLeft(Borders.BASIC_THIN_LINES);
        paragraph5.setBorderRight(Borders.BASIC_THIN_LINES);
        paragraph5.setBorderTop(Borders.BASIC_THIN_LINES);

        paragraph6.setAlignment(ParagraphAlignment.CENTER);
        paragraph6.setBorderBottom(Borders.BASIC_THIN_LINES);
        paragraph6.setBorderLeft(Borders.BASIC_THIN_LINES);
        paragraph6.setBorderRight(Borders.BASIC_THIN_LINES);
        paragraph6.setBorderTop(Borders.BASIC_THIN_LINES);

        paragraph7.setAlignment(ParagraphAlignment.LEFT);
        paragraph7.setBorderBottom(Borders.BASIC_THIN_LINES);
        paragraph7.setBorderLeft(Borders.BASIC_THIN_LINES);
        paragraph7.setBorderRight(Borders.BASIC_THIN_LINES);
        paragraph7.setBorderTop(Borders.BASIC_THIN_LINES);

        paragraph8.setAlignment(ParagraphAlignment.RIGHT);
        paragraph8.setBorderBottom(Borders.BASIC_THIN_LINES);
        paragraph8.setBorderLeft(Borders.BASIC_THIN_LINES);
        paragraph8.setBorderRight(Borders.BASIC_THIN_LINES);
        paragraph8.setBorderTop(Borders.BASIC_THIN_LINES);

        paragraph9.setBorderBottom(Borders.BASIC_THIN_LINES);
        paragraph9.setBorderLeft(Borders.BASIC_THIN_LINES);
        paragraph9.setBorderRight(Borders.BASIC_THIN_LINES);
        paragraph9.setBorderTop(Borders.BASIC_THIN_LINES);

        File file = generate();
        try(FileOutputStream output = new FileOutputStream(file)) {
            documentCont.write(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private File generate(){
        String path = Paths.C.get()+Paths.ACCOUNTING_SYSTEM.get()+Paths.DOCUMENTS.get()+Paths.ESMG_M.get();
        String prefix = "passport_esmgm11_";
        String postfix = ".docx";
        String genericTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fullname = prefix+genericTime+postfix;

        return new File(path,fullname);
    }
}

