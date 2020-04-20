package com.mtjin.data.source.search

import com.mtjin.data.mapper.mapperMovieListLocalToData
import com.mtjin.data.mapper.mapperMovieListRemoteToData
import com.mtjin.data.mapper.mapperMovieListRemoteToLocal
import com.mtjin.data.model.search.Movie
import com.mtjin.data.utils.NetworkManager
import com.mtjin.local.search.MovieLocalDataSource
import com.mtjin.remote.search.MovieRemoteDataSource

internal class MovieRepositoryImpl(
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
            // remote 검색 전 local에서 먼저 검색해서 데이터 전달
            with(movieLocalDataSource.getSearchMovies(query)) {
                if (this.isNotEmpty()) {
                    success(mapperMovieListLocalToData(this))
                }
            }
            // remote 에서 검색
            movieRemoteDataSource.getSearchMovies(
                query,
                success = {
                    // remote 성공시 remote 데이터 전달
                    movieLocalDataSource.insertMovies(mapperMovieListRemoteToLocal(it))
                    success(mapperMovieListRemoteToData(it))
                },
                fail = {
                    // remote 실패시 local 에서 검색
                    with(movieLocalDataSource.getSearchMovies(query)) {
                        if (this.isEmpty()) {
                            fail(it)
                        } else {
                            success(mapperMovieListLocalToData(this))
                        }
                    }
                }
            )
        } else {
            // local 에서 검색
            with(movieLocalDataSource.getSearchMovies(query)) {
                if (this.isEmpty()) {
                    fail(Throwable("해당 영화는 존재하지 않습니다.\n네트워크를 연결해서 검색해주세요"))
                } else {
                    success(mapperMovieListLocalToData(this))
                }
            }
        }
    }

    override fun getPagingMovies(
        query: String,
        start: Int,
        success: (List<Movie>) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        if (networkManager.checkNetworkState()) {
            movieRemoteDataSource.getSearchMovies(
                query,
                start,
                success = {
                    movieLocalDataSource.insertMovies(mapperMovieListRemoteToLocal(it))
                    success(mapperMovieListRemoteToData(it))
                },
                fail = {
                    fail(it)
                }
            )
        } else {
            fail(Throwable("네트워크가 연결이 되어있지 않습니다."))
        }
    }
}