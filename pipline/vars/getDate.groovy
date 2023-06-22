String call(String format = 'yyyy_MM_dd') {
    // formatting date
    f = new java.text.SimpleDateFormat(format, Locale.US)
    /* groovylint-disable-next-line NoJavaUtilDate */
    d = new Date(System.currentTimeMillis())
    postfix = f.format(d)

    return (String) "${postfix}"
}
