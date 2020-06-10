package com.summertaker.stock2

object Config {
    const val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:77.0) Gecko/20100101 Firefox/77.0"

    const val daumUrlKospi = "http://finance.daum.net/api/quotes/stocks?market=KOSPI&changes=UPPER_LIMIT,RISE,EVEN,FALL,LOWER_LIMIT"
    const val daumUrlKosdaq = "http://finance.daum.net/api/quotes/stocks?market=KOSDAQ&changes=UPPER_LIMIT,RISE,EVEN,FALL,LOWER_LIMIT"
}