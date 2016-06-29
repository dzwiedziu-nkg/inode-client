# inode-client

IoT data acquirer library for Java and Android.
This project is designed form [iNode][in] BTLE sensors. 

## Project parts

This project contains three modules: two libraries and one example app.

- **inode-client-core** &mdash; java library for encode and decode values and message frames,
- **inode-client-android** &mdash; Android library for communication with sensors via BTLE,
  supports API &ge;18
- **inode-client-example** &mdash; example Android app for test library.
  It can be discover sensors and show values from notification frame.
  Moreover it can download stored values and export to CSV file on POST to your REST service.

## Usage

Import to Android Studio and run **inode-client-example**. 

Please analyse inode-client-example project. Code is clean and having comments.
Any suggestions are welcome. 

## Project status

Current status: pre-alpha

### Done

- nothing :(

### Current TODO

I am willing to cooperate. If you want help to develop or testing this project please contact with me.

#### Library TODO

- decode values from notification frame,
- download stored values,
- erase stored values

##### Support care sensors

- [iNode Care Sensor PHT][pht]
- [iNode NAV][nav]

#### Example app TODO

- discover sensors,
- show values from notification frame,
- download values and store as CSV file,
- send CSV to REST service

### Future TODO

- support other [iNode][in] sensors,
- support login,
- support other sensors i.e. [Simplelink SensorTag][ti]

## License

Copyright (C) by Michał Niedźwiecki 2016  
License: GPL v3 but if you want to use it in your close source project please contact with me.  
Contact: nkg753 on gmail or via GitHub profile: dzwiedziu-nkg  

[in]: https://inode.pl/
[pht]: https://inode.pl/iNode-Care-Sensor-PHT-p34
[nav]: https://inode.pl/iNode-Nav,p,23
[ti]: http://www.ti.com/sensortag
