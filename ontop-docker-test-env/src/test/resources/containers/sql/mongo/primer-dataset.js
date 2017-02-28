//create the madMen database and connect to it
var db = connect('127.0.0.1:27017/madMen'),
    allMadMen = null;

//create the names collection and add documents to it
db.names.insert({'name' : 'Don Draper'});
db.names.insert({'name' : 'Peter Campbell'});
db.names.insert({'name' : 'Betty Draper'});
db.names.insert({'name' : 'Joan Harris'});

//set a reference to all documents in the database
allMadMen = db.names.find();

quit();