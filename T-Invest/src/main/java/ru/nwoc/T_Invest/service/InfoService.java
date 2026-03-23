package ru.nwoc.T_Invest.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.*;
import ru.ttech.piapi.core.connector.ServiceStubFactory;
import ru.ttech.piapi.core.connector.SyncStubWrapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class InfoService extends BaseTInvestService{

    private final MessageSenderImpl messageSender;

    private final SyncStubWrapper<InstrumentsServiceGrpc.InstrumentsServiceBlockingStub> instrumentsService;
    private final SyncStubWrapper<MarketDataServiceGrpc.MarketDataServiceBlockingStub> marketService;
    private final SyncStubWrapper<OperationsServiceGrpc.OperationsServiceBlockingStub> operationsService;
    private final SyncStubWrapper<UsersServiceGrpc.UsersServiceBlockingStub> usersService;

    public InfoService(ServiceStubFactory stubFactory, MessageSenderImpl messageSender) {
        super(stubFactory);
        this.messageSender = messageSender;
        this.instrumentsService = stubFactory.newSyncService(InstrumentsServiceGrpc::newBlockingStub);
        this.marketService = stubFactory.newSyncService(MarketDataServiceGrpc::newBlockingStub);
        this.operationsService = stubFactory.newSyncService(OperationsServiceGrpc::newBlockingStub);
        this.usersService = stubFactory.newSyncService(UsersServiceGrpc::newBlockingStub);
    }

    public void getInfo(String isin) {
        //SharesResponse response = instrumentsService.callSyncMethod(stub -> stub.shares(InstrumentsRequest.getDefaultInstance()));
        FindInstrumentRequest request = FindInstrumentRequest.newBuilder()
                .setInstrumentKind(InstrumentType.INSTRUMENT_TYPE_BOND)
                .setQuery(isin)
                .build();
        FindInstrumentResponse response = instrumentsService.callSyncMethod(stub->stub.findInstrument(request));
        if(response.getInstrumentsList().isEmpty()||!response.getInstrumentsList().getFirst().getInstrumentKind().equals(InstrumentType.INSTRUMENT_TYPE_BOND)){
            messageSender.sendMessage("Переданный isin не соответствует облигации");
        }else {
            String uid = response.getInstrumentsList().getFirst().getUid();
            Bond bond = findBondByUid(uid);

            log.info(bond.getName());
            List<Coupon> coupons = getCouponsByInstrumentUid(uid);
            Quotation price = getPriceByUid(uid);
            long days = getDaysBetween(bond.getMaturityDate());
            double coupon = Double.parseDouble(coupons.getFirst().getPayOneBond().getUnits() + "." + coupons.getFirst().getPayOneBond().getNano());
            double nkd = Double.parseDouble(bond.getAciValue().getUnits() + "." + bond.getAciValue().getNano());
            double currentPriceNoNKD = Double.parseDouble(price.getUnits() + "." + price.getNano()) * 10;
            int countCoupons = coupons.size();
            double nominal = Double.parseDouble(bond.getInitialNominal().getUnits() + "." + bond.getInitialNominal().getNano());
            double priceWithNKD = currentPriceNoNKD + nkd;
            double allPriceCoupons = countCoupons * coupon - nkd;
            double priceEndNominal = nominal - currentPriceNoNKD;
            double onePriceBond = (allPriceCoupons + priceEndNominal) - priceWithNKD * 0.003d;
            double years = (double) days / 365;
            double onePriceBondYear = onePriceBond / years;
            double percentYear = (onePriceBondYear * 100) / priceWithNKD;

            //System.out.println(response.getInstrumentsList());
            messageSender.sendMessage(bond.getName() + "\n" + "==========" + "\n" +
                    "Дней до погашения: " + days
                    + "\n" + "==========" + "\n" +
                    "Величина купона: " + coupon
                    + "\n" + "==========" + "\n" +
                    "НКД: " + nkd
                    + "\n" + "==========" + "\n" +
                    "Цена без НКД: " + currentPriceNoNKD
                    + "\n" + "==========" + "\n" +
                    "Цена с НКД: " + priceWithNKD
                    + "\n" + "==========" + "\n" +
                    "Осталось выплат по купонам: " + countCoupons
                    + "\n" + "==========" + "\n" +
                    "Размер номинала: " + nominal
                    + "\n" + "==========" + "\n" +
                    "Общая выплата купонов к закрытию: " + allPriceCoupons
                    + "\n" + "==========" + "\n" +
                    "Возмещение номинала: " + priceEndNominal
                    + "\n" + "==========" + "\n" +
                    "Профит с одной облигации к погашению: " + onePriceBond
                    + "\n" + "==========" + "\n" +
                    "Средний заработок в год к погашению: " + onePriceBondYear
                    + "\n" + "==========" + "\n" +
                    "Лет до погашения: " + years
                    + "\n" + "==========" + "\n" +
                    "Доходность облигации к погашению: " + percentYear
                    + "\n" + "==========" + "\n");
        }
    }
//    isin: "RU000A105CM4"
//    figi: "TCS00A105CM4"
//    ticker: "RU000A105CM4"
//    class_code: "TQCB"
//    instrument_type: "bond"
//    name: "\320\245\320\232 \320\235\320\276\320\262\320\276\321\202\321\200\320\260\320\275\321\201 001P-03"
//    uid: "b0b2c953-2508-4976-890d-33253b096bfb"
//    position_uid: "9b920fb4-9091-417d-a5d6-cd949be75075"
//    instrument_kind: INSTRUMENT_TYPE_BOND
//    api_trade_available_flag: true
//    for_iis_flag: true
//    first_1min_candle_date {
//        seconds: 1667309640
//    }
//    first_1day_candle_date {
//        seconds: 1667286000
//    }
//    lot: 1

    public InstrumentShort findInstrumentByQuery(String query){
        FindInstrumentRequest request = FindInstrumentRequest.newBuilder()
                .setQuery(query)
                //.setInstrumentKind(InstrumentType.INSTRUMENT_TYPE_BOND)
                .build();
        FindInstrumentResponse response = instrumentsService.callSyncMethod(stub->stub.findInstrument(request));
        return response.getInstruments(0);
    }

    public Bond findBondByUid(String uid){
        InstrumentRequest request = InstrumentRequest.newBuilder()
                .setIdType(InstrumentIdType.INSTRUMENT_ID_TYPE_UID)
                .setId(uid)
                .build();
        BondResponse response = instrumentsService.callSyncMethod(stub->stub.bondBy(request));
        return response.getInstrument();
    }

    public List<Coupon> getCouponsByInstrumentUid(String uid){
        Instant now = Instant.now();
        Instant then = Instant.now().plusSeconds(100L*365*24*60*60);
        Timestamp timestampFrom = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
        Timestamp timestampTo = Timestamp.newBuilder()
                .setSeconds(then.getEpochSecond())
                .setNanos(then.getNano())
                .build();
        GetBondCouponsRequest request = GetBondCouponsRequest.newBuilder()
                .setInstrumentId(uid)
                .setFrom(timestampFrom)
                .setTo(timestampTo)
                .build();
        GetBondCouponsResponse response = instrumentsService.callSyncMethod(stub->stub.getBondCoupons(request));
        return response.getEventsList();
    }

    public Quotation getPriceByUid(String uid){
        GetLastPricesRequest request = GetLastPricesRequest.newBuilder()
                .addInstrumentId(uid)
                .setLastPriceType(LastPriceType.LAST_PRICE_UNSPECIFIED)
                .setInstrumentStatus(InstrumentStatus.INSTRUMENT_STATUS_ALL)
                .build();
        GetLastPricesResponse response = marketService.callSyncMethod(stub->stub.getLastPrices(request));
        System.out.println(response.getLastPricesList().getFirst().getTime());
        return response.getLastPricesList().getFirst().getPrice();
    }
    private long getDaysBetween(Timestamp dateMaturity){
        Instant now = Instant.now();
        Instant dateMat = Instant.ofEpochSecond(dateMaturity.getSeconds(),dateMaturity.getNanos());
        return ChronoUnit.DAYS.between(now,dateMat);
    }

    public PortfolioResponse getPortfolioInfo(){
        PortfolioRequest request = PortfolioRequest.newBuilder()
                .setAccountId("")
                .setCurrency(PortfolioRequest.CurrencyRequest.RUB)
                .build();
        return operationsService.callSyncMethod(stub->stub.getPortfolio(request));
    }

    public GetAccountsResponse getAccounts(){
        GetAccountsRequest request = GetAccountsRequest.newBuilder()
                .setStatus(AccountStatus.ACCOUNT_STATUS_ALL)
                .build();
        GetAccountsResponse response = usersService.callSyncMethod(stub->stub.getAccounts(request));
        System.out.println(response);
        return response;
    }

    public GetInfoResponse getUserInfo(){
        GetInfoRequest request = GetInfoRequest.newBuilder().build();
        GetInfoResponse response = usersService.callSyncMethod(stub->stub.getInfo(request));
        System.out.println(response);
        return response;
    }

    public void getSchedule(){
        Instant now = Instant.now();
        Instant then = Instant.now().plusSeconds(2L*24*60*60);
        Timestamp timestampFrom = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
        Timestamp timestampTo = Timestamp.newBuilder()
                .setSeconds(then.getEpochSecond())
                .setNanos(then.getNano())
                .build();
        TradingSchedulesRequest request = TradingSchedulesRequest.newBuilder()
                .setFrom(timestampFrom)
                .setTo(timestampTo)
                .setExchange("MOEX_BONDS")
                .build();
        TradingSchedulesResponse response = instrumentsService.callSyncMethod(stub->stub.tradingSchedules(request));
        System.out.println(new Date(response.getExchangesList().getLast().getDaysList().getLast().getStartTime().getSeconds() * 1000));


    }
}