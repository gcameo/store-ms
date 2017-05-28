package sample.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.controlsfx.control.PopOver;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Created by Uran on 14/03/2017.
 */
public class Dashboard implements Initializable{

    DB db = new DB();
    Connection con = db.connect();

    @FXML VBox chartVb;
    @FXML private TableColumn sts, colPuna, colMng;
    @FXML private ComboBox cb;
    @FXML PieChart pieChart1, pieChart2, pieChart3, pieChart4;
    @FXML private Label lVleraHyratMuaj, lVleraHyratJave, lVleraHyratSot, lPnt, lPntA, lPntP, lKons, lShitje, lShitjeM, lProd, lProdSasia;
    @FXML NumberAxis xAxisA1, yAxisA1;
    @FXML private Hyperlink hlViti, hl6m, hl3m, hl1m, hl7d, hlSot;
    @FXML private PieChart pie1, pie2, pie3;
    @FXML private Label lPie1, lPie2, lPie3, lVleraHyratMuajTarget, lVleraHyratJaveTarget, lVleraHyratSotTarget;

    XYChart.Series series = new XYChart.Series();

    Random rand = new Random();

    NumberAxis y = new NumberAxis();
    NumberAxis x = new NumberAxis();
    AreaChart lineChart = new AreaChart(x, y);

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        getDataFromDb();

        fillPieCharts();

        lVleraHyratMuaj.setText(VariablatPublike.decimalFormat.format(VariablatPublike.hyratM));
        lVleraHyratJave.setText(VariablatPublike.decimalFormat.format(VariablatPublike.hyratJ));
        lVleraHyratSot.setText(VariablatPublike.decimalFormat.format(VariablatPublike.hyratS));
        lPnt.setText(VariablatPublike.pnt+"");
        lPntA.setText(VariablatPublike.pntA+"");
        lPntP.setText(VariablatPublike.pntP+"");
        lKons.setText(VariablatPublike.kons+"");
        lShitje.setText(VariablatPublike.shitje+"");
        lShitjeM.setText(VariablatPublike.decimalFormat.format(VariablatPublike.mes));
        lProd.setText(VariablatPublike.decimal.format(VariablatPublike.p));
        lProdSasia.setText(VariablatPublike.decimal.format(VariablatPublike.ps));

        lVleraHyratMuajTarget.setText(VariablatPublike.decimalFormat.format(VariablatPublike.muaj));
        lVleraHyratJaveTarget.setText(VariablatPublike.decimalFormat.format(VariablatPublike.jave));
        lVleraHyratSotTarget.setText(VariablatPublike.decimalFormat.format(VariablatPublike.dite));

        hlViti.setOnAction(e -> dataForAreaChart("vit"));
        hl6m.setOnAction(e -> dataForAreaChart("6m"));
        hl3m.setOnAction(e -> dataForAreaChart("3m"));
        hl1m.setOnAction(e -> dataForAreaChart("1m"));
        hl7d.setOnAction(e -> dataForAreaChart("7d"));
        hlSot.setOnAction(e -> dataForAreaChart("sot"));

        displayChart();
    }

    private void displayChart(){

        lineChart.setAnimated(false);
        dataForAreaChart("sot");
//        lineChart.getData().add(series);
        chartVb.getChildren().add(1, lineChart);

        lineChart.getData().add(series);
        Axis<Double> xa = lineChart.getXAxis();

        Tooltip tp = new Tooltip();
        tp.getStyleClass().add("chart-tooltipPop");
        lineChart.lookup(".chart-plot-background").setOnMouseMoved(event -> {
            try {
                Events(xa, series, tp, event, ".chart-plot-background");
            }catch (IndexOutOfBoundsException iobe) {}
        });

        lineChart.lookup(".chart-series-area-fill").setOnMouseMoved(e -> {
            try {
                Events(xa, series, tp, e, ".chart-series-area-fill");
            }catch (Exception ex) {}
        });

        series.getNode().setOnMouseExited(e -> {
            series.getNode().setStyle("-fx-background-color: transparent");
        });

        lineChart.setOnMouseExited(e -> {
            tp.hide();
        });

        lineChart.setLegendVisible(false);
        lineChart.setHorizontalGridLinesVisible(false);
        x.setTickLabelsVisible(false);
        x.setTickUnit(1);
        x.setMinorTickVisible(false);
        x.setStyle("-fx-opacity: .9; -fx-fill: #000");
    }

    private void Events (Axis<Double> xa, XYChart.Series series, Tooltip tp, MouseEvent event, String lookup) {

        Node n = ((XYChart.Data)series.getData().get((int)xa.toNumericValue(xa.getValueForDisplay(event.getX())+0.5)-1)).getNode();

        tp.setText(VariablatPublike.dataMap.get((xa.getValueForDisplay(n.getLayoutX()).intValue()+1)) + "\nTe hyrat per kete date: " +
                VariablatPublike.decimalFormat.format(((XYChart.Data)series.getData().get((int)xa.toNumericValue(xa.getValueForDisplay(n.getLayoutX())))).getYValue()));

        tp.show(lineChart.lookup(lookup),
                n.localToScreen(n.getBoundsInLocal()).getMinX()-tp.getWidth()/2,
                n.localToScreen(n.getBoundsInLocal()).getMinY()-tp.getHeight());
    }

    private String cap(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1, text.length()).toLowerCase();
    }

    private boolean leapYear(int viti) {
        if (viti % 4 == 0) {
            if (viti % 200 == 0)
                return false;
            else if (viti % 400 == 0)
                return true;
            return true;
        }
        return false;
    }

    private void getDataFromDb(){
        try {
            Statement stmt = con.createStatement();
            Statement stmt2 = con.createStatement();
            Statement stmt3 = con.createStatement();
            Statement stmt4 = con.createStatement();
            Statement stmt5 = con.createStatement();
            Statement stmt6 = con.createStatement();
            Statement stmt7 = con.createStatement();
            Statement stmt8 = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from te_hyrat limit 1");
            ResultSet rs2 = stmt2.executeQuery("select * from pnt limit 1");
            ResultSet rs3 = stmt3.executeQuery("select count(*) from konsumatoret limit 1");
            ResultSet rs4 = stmt4.executeQuery("select count(distinct rec_id) from shitjet limit 1");
            ResultSet rs5 = stmt5.executeQuery("select avg(qmimi_shitjes) from vshitjet limit 1");
            ResultSet rs6 = stmt6.executeQuery("select count(*) as c, sum(sasia) as s from produktet limit 1");
            ResultSet rs7 = stmt7.executeQuery("select kategoria as k, id from kat_prod");
            ResultSet rs8 = stmt8.executeQuery("select emri, id from konsumatoret order by id");

            rs.next();
            VariablatPublike.hyratM = rs.getDouble("muaj");
            VariablatPublike.hyratJ = rs.getDouble("java");
            VariablatPublike.hyratS = rs.getDouble("sot");

            rs2.next();
            VariablatPublike.pnt = rs2.getInt("nr");
            VariablatPublike.pntA = rs2.getInt("a");
            VariablatPublike.pntP = rs2.getInt("p");

            rs3.next();
            VariablatPublike.kons = rs3.getInt("count(*)");

            rs4.next();
            VariablatPublike.shitje = rs4.getInt("count(distinct rec_id)");

            rs5.next();
            VariablatPublike.mes = rs5.getDouble("avg(qmimi_shitjes)");

            rs6.next();
            VariablatPublike.p = rs6.getInt("c");
            VariablatPublike.ps = rs6.getInt("s");

            while (rs7.next()) {
                VariablatPublike.prodKat.add(rs7.getString("k"));
                VariablatPublike.mProdKat.put(rs7.getInt("id"), rs7.getString("k"));
                VariablatPublike.revProdKat.put(rs7.getString("k"), rs7.getInt("id"));
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dataForAreaChart(String p){
        try {

            Statement statement = con.createStatement();
            String q = null;

            if (p.equals("vit")) {
                q = "select cast(koha_shitjes as date) as data, sum(totalneto) as qmimi_shitjes from vshitjet " +
                        "where koha_shitjes >= dateadd('month', -12, current_date()) " +
                        "group by data";
            }else if (p.equals("6m")) {
                q = "select sum(totalneto) as qmimi_shitjes, cast(koha_shitjes as date) as data from vshitjet " +
                        "where koha_shitjes >= dateadd('month', -6, current_date()) group by data";
            }else if (p.equals("3m")) {
                q = "select sum(totalneto) as qmimi_shitjes, cast(koha_shitjes as date) as data from vshitjet " +
                        "where koha_shitjes >= dateadd('month', -3, current_date()) group by data";
            }else if (p.equals("1m")) {
                q = "select sum(totalneto) as qmimi_shitjes, cast(koha_shitjes as date) as data from vshitjet " +
                        "where koha_shitjes >= dateadd('month', -1, current_date()) group by data";
            }else if (p.equals("7d")) {
                q = "select sum(totalneto) as qmimi_shitjes, cast(koha_shitjes as date) as data from vshitjet where " +
                        "koha_shitjes >= dateadd('day', -7, current_date()) " +
                        "group by data";
            }else {
                q = "select sum(totalneto) as qmimi_shitjes, (hour(koha_shitjes) || ':00') as data from vshitjet where " +
                        "cast(koha_shitjes as date) = current_date() " +
                        "group by data";
            }

            ResultSet rs = statement.executeQuery(q + " order by data");
            series.getData().clear();
            int i = 0;
            while (rs.next()) {
                series.getData().add(new XYChart.Data(++i, rs.getDouble("qmimi_shitjes")));
                VariablatPublike.dataMap.put(i, !p.equals("sot") ? VariablatPublike.sdf.format(rs.getDate("data")) : rs.getString("data"));
            }

        }catch (Exception e) {e.printStackTrace();}
    }

    private void fillPieCharts (){

        getPercentage();

        int m = (int)(VariablatPublike.hyratM / VariablatPublike.muaj * 100);
        int j = (int)(VariablatPublike.hyratJ / VariablatPublike.jave * 100);
        int d = (int)(VariablatPublike.hyratS / VariablatPublike.dite * 100);

        if (m >= 100) {
            lPie1.setText("");
            lPie1.setGraphic(new ImageView(new Image("/sample/photo/done.png")));
            pie1.getData().add(new PieChart.Data("", 100));
            pie1.getData().add(new PieChart.Data("", 0));
        }else {
            lPie1.setText(m + "%");
            pie1.getData().add(new PieChart.Data("", m));
            pie1.getData().add(new PieChart.Data("", 100 - m));
        }


        if (j >= 100) {
            lPie2.setText("");
            lPie2.setGraphic(new ImageView(new Image("/sample/photo/done.png")));
            pie2.getData().add(new PieChart.Data("", 100));
            pie2.getData().add(new PieChart.Data("", 0));
        }else {
            lPie2.setText(j + "%");
            pie2.getData().add(new PieChart.Data("", j));
            pie2.getData().add(new PieChart.Data("", 100 - j));
        }

        if (d >= 100) {
            lPie3.setText("");
            lPie3.setGraphic(new ImageView(new Image("/sample/photo/done.png")));
            pie3.getData().add(new PieChart.Data("", 100));
            pie3.getData().add(new PieChart.Data("", 0));
        }else {
            lPie3.setText(d + "%");
            pie3.getData().add(new PieChart.Data("", d));
            pie3.getData().add(new PieChart.Data("", 100 - d));
        }
    }

    private void getPercentage() {
        try {

            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("select * from target");

            while (rs.next()) {
                if (rs.getInt("tipi") == 1) {
                    VariablatPublike.muaj = rs.getDouble("qmimi") *
                            VariablatPublike.javetNeMuaj(VariablatPublike.ditetMuajit(VariablatPublike.leapYear(LocalDate.now().getYear()),
                            Calendar.getInstance().get(Calendar.MONTH)+1));
                    VariablatPublike.jave = rs.getDouble("qmimi");
                    VariablatPublike.dite = rs.getDouble("qmimi") / 7;
                }else if (rs.getInt("tipi") == 0) {
                    VariablatPublike.muaj = rs.getDouble("qmimi");
                    VariablatPublike.jave = rs.getDouble("qmimi") /
                            VariablatPublike.ditetMuajit(VariablatPublike.leapYear(LocalDate.now().getYear()),
                                    Calendar.getInstance().get(Calendar.MONTH)+1) * 7;
                    VariablatPublike.dite = rs.getDouble("qmimi") /
                            VariablatPublike.ditetMuajit(VariablatPublike.leapYear(LocalDate.now().getYear()),
                            Calendar.getInstance().get(Calendar.MONTH)+1);
                }else {
                    VariablatPublike.dite = rs.getDouble("qmimi");
                    VariablatPublike.jave = rs.getDouble("qmimi") * 7;
                    VariablatPublike.muaj = rs.getDouble("qmimi") * VariablatPublike.ditetMuajit(VariablatPublike.leapYear(LocalDate.now().getYear()),
                            Calendar.getInstance().get(Calendar.MONTH)+1);
                }
            }

        }catch (Exception e ){ e.printStackTrace();}
    }



}
