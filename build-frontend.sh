#!/bin/bash

cd ./frontend

npm install

ng build -prod

cd ..

cp ./frontend/dist/inline.*.bundle.js ./hat/app/org/hatdex/hat/phata/assets/js/inline.bundle.js

cp ./frontend/dist/polyfills.*.bundle.js ./hat/app/org/hatdex/hat/phata/assets/js/polyfills.bundle.js

cp ./frontend/dist/main.*.bundle.js ./hat/app/org/hatdex/hat/phata/assets/js/main.bundle.js

cp ./frontend/dist/vendor.*.bundle.js ./hat/app/org/hatdex/hat/phata/assets/js/vendor.bundle.js

cp ./frontend/dist/styles.*.bundle.css ./hat/app/org/hatdex/hat/phata/assets/stylesheets/styles.bundle.css
