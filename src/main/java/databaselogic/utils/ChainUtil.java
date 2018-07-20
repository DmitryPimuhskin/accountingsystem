package databaselogic.utils;

import com.sun.istack.internal.NotNull;
import domain.Balance;
import domain.DetailElectrod;
import domain.Electrod;
import domain.ElectrodeSummary;
import entities.*;
import services.AccoutingHistoryService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ChainUtil {

    public static List<DetailElectrod> createChainElectrodeDetail(List<Detail> details, List<Electrod> electrods, List<PrimitiveElectrodeDetail> primitivs) {
        List<DetailElectrod> detailElectrods = new ArrayList<>();
        for (PrimitiveElectrodeDetail primitive : primitivs) {
            DetailElectrod de = new DetailElectrod();
            Detail detail = details.stream().findFirst().filter(e -> e.getId() == primitive.getIdDetail()).get();
            Electrod electrod = electrods.stream().filter(e -> e.getId() == primitive.getIdElectrode()).findFirst().get();
            de.setElectrod(electrod);
            de.setDetail(detail);
            de.setCountDetailForElectrode(primitive.getCount());
            detailElectrods.add(de);
        }
        if (!detailElectrods.isEmpty())
            return detailElectrods;
        return null;
    }

    public static void createAccHistoryChain(List<Detail> details, List<AccoutingHistory> histories) {
        details.forEach(detail -> {
            histories.stream().filter(h -> h.getIdDetail() == detail.getId()).forEach(history -> history.setDetail(detail));
        });
    }

    public static Map<Detail, Map<PrimitivityBalance, List<AccoutingHistory>>> associateDetailWithPrimitiveAndHistory(List<Detail> details, List<PrimitivityBalance> pBalances, List<AccoutingHistory> histories) {
        createAccHistoryChain(details, histories);

        Map<Detail, Map<PrimitivityBalance, List<AccoutingHistory>>> associations = new HashMap<>();

        details.forEach(detail -> {
            Map<PrimitivityBalance, List<AccoutingHistory>> pBalanceAccHist = new HashMap<>();
            // filter by detail
            List<PrimitivityBalance> personalPrimBalance = pBalances.stream().filter(p -> p.getIdDetail() == detail.getId()).collect(Collectors.toList());
            List<AccoutingHistory> personalHistory = histories.stream().filter(h -> h.getIdDetail() == detail.getId()).collect(Collectors.toList());
            // reference primitiv balance with history by month
            personalPrimBalance.forEach(p -> {
                pBalanceAccHist.put(
                        p,
                        personalHistory.stream().filter(h -> h.getMonth().equals(p.getMonth())).collect(Collectors.toList())
                );
            });
            if (!personalPrimBalance.isEmpty())
                associations.put(detail, pBalanceAccHist);
        });

        associations.forEach((d, pa) -> {
            pa.forEach((p, h) -> {
                double[] sums = AccoutingHistoryService.calculate(h);
                p.setIncoming(sums[0]);
                p.setOutcoming(sums[1]);
            });
        });

        return associations;
    }

    public static List<Balance> createBalanceChain(List<Detail> details, List<PrimitivityBalance> pBalances, List<AccoutingHistory> histories) {
        List<Balance> balances = new ArrayList<>();

        Map<Detail, Map<PrimitivityBalance, List<AccoutingHistory>>> associations = associateDetailWithPrimitiveAndHistory(details, pBalances, histories);

        associations.forEach((d, pa) -> {
            Balance balance = new Balance();
            balance.setId(d.getId());
            balance.setDetail(d);

            AtomicReference<Double> incTotal = new AtomicReference<>(0.0);
            AtomicReference<Double> outTotal = new AtomicReference<>(0.0);
            AtomicReference<Double> begYear = new AtomicReference<>(0.0);
            Double endYear = 0.0;

            Map<Month, Double> inc = new HashMap<>();
            Map<Month, Double> out = new HashMap<>();

            pa.forEach((p, h) -> {
                inc.put(p.getMonth(), p.getIncoming());
                out.put(p.getMonth(), p.getOutcoming());
                incTotal.updateAndGet(v -> v + p.getIncoming());
                outTotal.updateAndGet(v -> v + p.getOutcoming());
                begYear.set(p.getBalanceAtBeginningYear());

            });
            // round to 3 symbols after dot
            begYear.set(new BigDecimal(begYear.get()).setScale(3, RoundingMode.UP).doubleValue());
            endYear = new BigDecimal(incTotal.get() - outTotal.get()).setScale(3, RoundingMode.UP).doubleValue();

            balance.setBalanceAtBeginningYear(begYear.get());
            balance.setBalanceAtEndOfYear(endYear);
            balance.setInTotal(incTotal.get());
            balance.setOutTotal(outTotal.get());
            balance.setIncoming(inc);
            balance.setOutcoming(out);

            balances.add(balance);
        });
        return balances;
    }

    public static List<ElectrodeSummary> createElectrodeSummaryChain(List<Electrod> electrods, List<Summary> summaries) {
        List<ElectrodeSummary> es = new ArrayList<>();

        for (Electrod electrode : electrods) {
            ElectrodeSummary elEs = new ElectrodeSummary();
            Summary summary = summaries.stream().filter(s -> s.getIdElectrode().equals(electrode.getId())).findFirst().get();
            elEs.setElectrod(electrode);
            elEs.setSummary(summary);
            es.add(elEs);
        }
        if (!es.isEmpty())
            return es;
        return null;
    }

    public static void associateDetailWithHistory(Detail detail, List<AccoutingHistory> histories) {
        histories.forEach(history -> history.setDetail(detail));
    }
}
