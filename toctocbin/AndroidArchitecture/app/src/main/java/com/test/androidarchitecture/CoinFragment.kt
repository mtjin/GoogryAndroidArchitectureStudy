package com.test.androidarchitecture


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.test.androidarchitecture.data.Coin
import com.test.androidarchitecture.network.RetrofitClient
import com.test.androidarchitecture.network.RetrofitService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class CoinFragment : Fragment() {

    private var coinList: ArrayList<String>? = null
    private var retrofitService: RetrofitService? = null
    private var coinType: String? = null

    companion object {

        fun getInstance(coinFilterList: ArrayList<String>, coinType: String): CoinFragment {
            val args = Bundle()
            args.putStringArrayList("coinList", coinFilterList)
            args.putString("coinType", coinType)
            val fragment = CoinFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coinList = arguments?.getStringArrayList("coinList")
        coinType = arguments?.getString("coinType")
        coinList?.let { loadCoinData(it) }
    }


    private fun loadCoinData(coinList: ArrayList<String>) {
        retrofitService = RetrofitClient().getClient().create(RetrofitService::class.java)
        retrofitService?.loadCoinData(coinList.joinToString(","))?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(this::coinResponse, this::coinError)
    }

    private fun coinResponse(coinList: ArrayList<Coin>) {
        Toast.makeText(context, coinList[0].low_price, Toast.LENGTH_SHORT).show()
    }

    private fun coinError(error: Throwable) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
    }

}
