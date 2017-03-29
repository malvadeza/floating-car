package io.github.malvadeza.floatingcar.data.source

class ObdRepository private constructor(private val localDataSource: ObdDataSource) : ObdDataSource {
    companion object {

        private var instance: ObdDataSource? = null

        fun getInstance(localDataSource: ObdRepository): ObdDataSource {
            if (instance == null)
                instance = ObdRepository(localDataSource)


            return instance!!
        }
    }

}
