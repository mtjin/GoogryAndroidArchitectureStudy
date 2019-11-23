package com.god.taeiim.myapplication.ui

import com.god.taeiim.myapplication.data.NaverRepositoryImpl

class ContentsPresenter(private val view: ContentsContract.View) : ContentsContract.Presenter {

    override fun searchContents(searchType: String, query: String) {
        if (query.isBlank()) {
            view.blankSearchQuery()

        } else {
            NaverRepositoryImpl.getResultData(
                searchType,
                query,
                success = { view.updateItems(it) },
                fail = { view.failToSearch() }
            )
        }
    }

}