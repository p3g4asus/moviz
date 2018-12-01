https://www.googleapis.com/fitness/v1/users/me/dataSources/raw:com.google.heart_rate.bpm:407408718192:geonaute:BLE HR Device:2d99a343:/datasets/1465704112000000000-1465704118000000000
{
"dataSourceId":"raw:com.google.heart_rate.bpm:407408718192:geonaute:BLE HR Device:2d99a343:",
    "maxEndTimeNs": 1465704113000000000,
  "minStartTimeNs": 1465704112000000000,
"point": [
    {
      "startTimeNanos": "1465704112000000000", 
      "endTimeNanos": "1465704112000000000", 
      "value": [
        {
          "mapVal": [], 
          "fpVal": 133.80000305175781
        }
      ], 
      "dataTypeName": "com.google.heart_rate.bpm", 
      "originDataSourceId": ""
    }, 
    {
      "startTimeNanos": "1465704113000000000", 
      "endTimeNanos": "1465704113000000000", 
      "value": [
        {
          "mapVal": [], 
          "fpVal": 135.19999694824219
        }
      ], 
      "dataTypeName": "com.google.heart_rate.bpm", 
      "originDataSourceId": ""
    }
]
}

{
      "name": "PROVAHR", 
      "dataStreamName": "", 
      "dataType": {
        "field": [
          {
            "name": "bpm", 
            "format": "floatPoint"
          }
        ], 
        "name": "com.google.heart_rate.bpm"
      }, 
      "dataQualityStandard": [], 
      "application": {
        "name": "MFZ REST TST"
      }, 
      "device": {
        "model": "BLE HR Device", 
        "version": "", 
        "type": "unknown", 
        "uid": "2d99a343", 
        "manufacturer": "geonaute"
      }, 
      "type": "raw"
    }
    
    
    {
  "name": "PROVAHR", 
  "dataStreamName": "", 
  "dataType": {
    "field": [
      {
        "name": "bpm", 
        "format": "floatPoint"
      }
    ], 
    "name": "com.google.heart_rate.bpm"
  }, 
  "dataQualityStandard": [], 
  "application": {
    "name": "MFZ REST TST"
  }, 
  "device": {
    "model": "BLE HR Device", 
    "version": "", 
    "type": "unknown", 
    "uid": "2d99a343", 
    "manufacturer": "geonaute"
  }, 
  "dataStreamId": "raw:com.google.heart_rate.bpm:407408718192:geonaute:BLE HR Device:2d99a343:", 
  "type": "raw"
}

{
  "error": {
    "code": 401, 
    "message": "Invalid Credentials", 
    "errors": [
      {
        "locationType": "header", 
        "domain": "global", 
        "message": "Invalid Credentials", 
        "reason": "authError", 
        "location": "Authorization"
      }
    ]
  }
}
