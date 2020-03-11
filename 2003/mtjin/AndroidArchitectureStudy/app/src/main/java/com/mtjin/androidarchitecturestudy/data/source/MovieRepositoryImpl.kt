package com.mtjin.androidarchitecturestudy.data.source

import com.mtjin.androidarchitecturestudy.data.Movie
import com.mtjin.androidarchitecturestudy.data.source.local.MovieLocalDataSource
import com.mtjin.androidarchitecturestudy.data.source.remote.MovieRemoteDataSource
import com.mtjin.androidarchitecturestudy.utils.NetworkManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MovieRepositoryImpl(
    private val movieRemoteDataSource: MovieRemoteDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val networkManager: NetworkManager
) : MovieRepository {

    override fun getSearchMovies(
        query: String,
        success: (List<Movie>) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        if (networkManager.checkNetworkState()) {
            // remote 에서 검색
            movieRemoteDataSource.getSearchMovies(
                query,
                success = {
                    // remote 성공시 remote 데이터 전달
                    runBlocking {
                        launch {
                            movieLocalDataSource.insertMovies(it)
                            success(it)
                        }
                    }
                },
                fail = {
                    // remote 실패시 local 에서 검색
                    runBlocking {
                        launch {
                            with(movieLocalDataSource.getSearchMovies(query)) {
                                if (this.isEmpty()) {
                                    fail(it)
                                } else {
                                    success(this)
                                }
                            }
                        }
                    }
                }
            )
        } else {
            // local 에서 검색
            runBlocking {
                launch {
                    with(movieLocalDataSource.getSearchMovies(query)) {
                        if (this.isEmpty()) {
                            fail(Throwable("해당 영화는 존재하지 않습니다."))
                        } else {
                            success(this)
                        }
                    }
                }
            }
        }
    }
}