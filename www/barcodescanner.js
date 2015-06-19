cordova.define("cordova/plugin/barcodescanner", 
  function(require, exports, module) {
    var exec = require("cordova/exec");
    var BarcodeScanner = function() {};
    
  /**
   * Check if the device has a possibility to receive DataWedge output
   * the successCallback function receives one string as parameter
   */
  BarcodeScanner.prototype.startBarcodeReceiving = function(successCallback, failureCallback) {
      
      exec(successCallback, failureCallback, 'barcodescanner', 'startBarcodeReceiving', []);
    
  };

  /**
   * Stop Receiving.
   */
  BarcodeScanner.prototype.stopBarcodeReceiving = function(successCallback,failureCallback) {
    exec(successCallback, failureCallback, 'barcodescanner', 'stopBarcodeReceiving', []);
  };

    
    var BarcodeObj = new BarcodeScanner();
    module.exports = BarcodeObj;

});


if(!window.plugins) {
    window.plugins = {};
}
if (!window.plugins.BarcodeScanner) {
    window.plugins.BarcodeScanner = cordova.require("cordova/plugin/barcodescanner");
}