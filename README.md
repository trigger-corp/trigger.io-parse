# parse module for Trigger.io

This repository holds everything required to build the parse [Trigger.io](https://trigger.io/) module.

For more information about working on Trigger.io native modules, see [the documentation](https://trigger.io/docs/current/api/native_modules/index.html).


## Building parse

    git clone https://github.com/parse-community/Parse-SDK-iOS-OSX.git Parse-SDK-iOS-OSX.git
    cd Parse-SDK-iOS-OSX.git
    git submodule update --init --recursive

    source /Users/antoine/.rvm/scripts/rvm
    rvm install "ruby-2.3.7-parse"
    rvm use 2.3.7-parse

    gem install bundler
    bundle install
    bundle exec rake package:frameworks
