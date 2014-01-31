# heroku-clojure-rest

A Clojure library designed to ... well, that part is up to you.


## Heroku deploy:
First, modify core.clj to indicate which is the index.html, in this case "production.html"
> lein clean
> git add .
> git commit -am "heroku deployment"
> git push
> git push heroku master
> lein cljsbuild once release
> lein uberjar
> git push heroku master
> git open


## Usage

FIXME

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
