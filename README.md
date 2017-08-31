
Untangled Phone numbers application
-----------------------------------

The same code as used to demonstrate Untangled's new forms capabilities, just done as full stack, so no mocking the
server in cljs code.

Setup the same as for any other Untangled full-stack application. In IntelliJ setup Server and Figwheel configurations,
 just as demonstrated in the videos. You merely need to create the Server configuration. For Figwheel (i.e. client) you need to set
  parameters to `script/figwheel.clj`. Once those two are setup `(go)` or `(reset)` in the Server config's JVM, and merely
  have the Figwheel JVM/server running. Browse at `http://localhost:8080/`.

Irrelevant
----------

# csv-to-csv
When headings need to be mapped across and if you have to lose data (because fewer headings on target) you want to see it
