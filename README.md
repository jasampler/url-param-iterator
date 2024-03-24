# URLParamIterator

Java utility class for traversing the query parameters of any given URL like `http://site/path?key1=value1&key2=value2` and optionally modifying them.

Notes:
* This utility does not decode/encode the keys/values so that must be done by the user for getKey()/getValue() and insertXxx() when appropriate.
* Parameter keys cannot be null, but parameter values will be null when the value separator '=' is not found.
* If the last parameter is reached, calling again to next() has no effect.
* Insertions/removals do not affect the iteration order of the initial URL.
* Each parameter added using any insertion method will be concatenated _after_ the previous parameters added with the same method.
* Inserting a parameter with a null key will throw a NullPointerException.

Examples:
```
URLParamIterator it = new URLParamIterator("AA?k=v&k=&k&=&");
while (it.next()) {
    System.out.println(it.getKey() + "=" + it.getValue());
}
//prints the lines: k=v, k=, k=null, =, =null

URLParamIterator it = new URLParamIterator("http://site?c=2&d");
while (it.next()) {
    if (it.isFirst()) {
        it.insertBefore("b", "1"); //inserts a parameter BEFORE current one
    }
    if (it.getKey().equals("d") && ! "3".equals(it.getValue())) {
        //to modify a parameter it must be removed and reinserted:
        it.remove();
        it.insertBefore("d", "3"); //insertAfter() could be used too
    }
    if (it.isLast()) {
        it.insertAfter("e", "4"); //inserts a parameter AFTER current one
    }
}
it.insertFirst("a", "0"); //inserts a parameter at the beginning
it.insertLast("f", "5"); //inserts a parameter at the end
//it.toString(): "http://site?a=0&b=1&c=2&d=3&e=4&f=5"
```

Copyright 2024 Carlos Rica ( jasampler AT gmail DOT com )
