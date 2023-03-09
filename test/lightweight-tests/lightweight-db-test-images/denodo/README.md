# Testing Denodo using Lightweight Tests

1) Run the _pgsql_ docker image from the lightweight test directory.
2) Install _Denodo Express Community Edition_.
3) Start the Denodo Express Virtual DataPort Administration Tool:
   1) Through the terminal, in the directory `denodo-platform-x.y`, run `-/bin/denodo_platform.sh`.
   2) In the UI, switch to the tab `Virtual DataPort`.
   3) Under `Servers` -> `Virtual DataPort Server` press `Start`.
   4) Press the `LAUNCH` button at the bottom of the window.
   5) The admin tool will now open up.
4) Import the `vql` files:
   1) In the admin tool, go to `File` -> `Import`.
   2) Next to `VQL file` press `Browse`.
   3) Select the `vql` file you want to import.
   4) Press `Ok`.
   5) Repeat this for all three `vql` files.
5) The lightweight tests can now be executed.