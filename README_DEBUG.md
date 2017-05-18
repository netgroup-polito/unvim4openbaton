# DEBUG information

## Screen commands
By default, both Open Baton orchestrator and the generic VNFM are executed in a screen console.
Following some useful commands:
- Show all the screen instances active
  ```sh
  screen -ls
  ```
- Enter inside the screen console
  ```sh
  screen -x
  ```
- Exit from the screen views without closing the screen instances
    ```sh
    CTRL + A - D
    ```
- Change screen instance inside the console
    ```sh
    CTRL + A - $number
    ```
  where '$number' is the number of the screen instance you want to switch to (you can check the instances' number at the bottom of the console)
- Scroll back the console log (by default you can't scroll it)
    ```sh
    CTRL + A - :scrollback $number
    ```
    where '$number' is the numebr of line you can scroll back
    
## Log file
You can find:
* Plugin log in
    ```sh
    /opt/openbaton/nfvo/plugin-logs
* Open Baton orchestrator and Generic VNFM logs in 
    ```sh
    /var/log/openbaton
    ```
