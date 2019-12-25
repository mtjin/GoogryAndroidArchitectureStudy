package wooooooak.com.studyapp.ui.kin


import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import wooooooak.com.studyapp.R
import wooooooak.com.studyapp.common.ext.startWebView
import wooooooak.com.studyapp.data.model.repository.NaverApiRepositoryImpl
import wooooooak.com.studyapp.data.model.response.kin.Kin
import wooooooak.com.studyapp.ui.base.BaseSearchListAdapter
import wooooooak.com.studyapp.ui.base.ItemSearchFragment

class KinFragment : ItemSearchFragment<Kin>(R.layout.fragment_kin) {

    override val adapter = KinListAdapter(object : BaseSearchListAdapter.ItemListener<Kin> {
        override fun loadMoreItems(list: List<Kin>, index: Int) {
            lifecycleScope.launch {
                presenter.fetchMoreItems(list, index)
            }
        }

        override fun renderWebView(url: String) {
            requireContext().startWebView(url)
        }
    })

    private val presenter by lazy { KinPresenter(this, NaverApiRepositoryImpl) }

    override fun initItemsByTitle(title: String) {
        lifecycleScope.launch {
            presenter.fetchItemsWithNewTitle(title)
        }
    }

}