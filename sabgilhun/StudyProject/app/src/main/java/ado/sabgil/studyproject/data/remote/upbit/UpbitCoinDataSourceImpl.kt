package ado.sabgil.studyproject.data.remote.upbit

import ado.sabgil.studyproject.data.model.Ticker
import ado.sabgil.studyproject.data.remote.CoinDataSource
import ado.sabgil.studyproject.data.remote.upbit.request.UpbitTickerListRequest
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object UpbitCoinDataSourceImpl : CoinDataSource {

    private val retrofit: UpbitApi

    private const val baseURL = "https://api.upbit.com/v1/"

    private var cachedTickerListRequest: UpbitTickerListRequest? = null

    private var behaviorSubject: BehaviorSubject<List<Ticker>>? = null

    private var subscribeCnt = 0

    private val compositeDisposable = CompositeDisposable()

    init {
        retrofit = run {
            Retrofit.Builder().baseUrl(baseURL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UpbitApi::class.java)
        }
    }

    override fun loadMarketList(): Single<List<String>> {
        return retrofit.loadMarketCode()
            .map { response ->
                cachedTickerListRequest = UpbitTickerListRequest.fromResponse(response)
                response.map {
                    it.market.substringBefore('-')
                }.distinct()
            }
    }

    override fun subscribeCoinDataByCurrency(
        baseCurrency: String
    ): Observable<List<Ticker>> {
        return subscribeCoinData()
            .map { tickers ->
                tickers.filter {
                    it.base == baseCurrency
                }
            }
    }

    override fun subscribeCoinDataByCoinName(
        coinName: String
    ): Observable<List<Ticker>> {
        return subscribeCoinData()
            .map { tickers ->
                tickers.filter {
                    it.coinName == coinName
                }
            }
    }

    override fun unSubscribeCoinData() {
        subscribeCnt--
        if (subscribeCnt == 0) {
            compositeDisposable.clear()
            behaviorSubject = null
        }
    }

    private fun subscribeCoinData(): Observable<List<Ticker>> {
        var behaviorSubject = this.behaviorSubject

        if (behaviorSubject == null) {
            behaviorSubject = BehaviorSubject.create<List<Ticker>>()

            Observable.interval(0, 5000, TimeUnit.MILLISECONDS)
                .flatMap {
                    if (cachedTickerListRequest != null) {
                        loadAllTicker(cachedTickerListRequest!!).toObservable()
                    } else {
                        loadMarketList()
                            .flatMap {
                                loadAllTicker(cachedTickerListRequest!!)
                            }.toObservable()
                    }
                }
                .subscribe(
                    behaviorSubject::onNext,
                    behaviorSubject::onError
                )
                .addTo(compositeDisposable)
        }
        subscribeCnt++

        return behaviorSubject
    }

    private fun loadAllTicker(
        request: UpbitTickerListRequest
    ): Single<List<Ticker>> {
        return Single.fromObservable(
            retrofit.loadTickerList(request.marketCodeQuery)
                .map { response ->
                    response.map {
                        Ticker.fromApiResponse(it)
                    }
                })
    }
}