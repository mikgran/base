var express = require('express');
var app = express();
var mongojs = require('mongojs');
// mongojs("127.0.0.1:27017/"+db, collections);
var db = mongojs('localhost/contactlist', ['contactlist']);
var bodyParser = require('body-parser');

db.on('error', function (err) {
    console.log('database error', err)
});

db.on('connect', function () {
    console.log('database connected')
})

/*
app.get('/', function(req, res) {

    res.send("Hello world from server.js");
});
*/

app.use(express.static(__dirname + "/public"));
app.use(bodyParser.json());

app.get('/contactlist', function (req, res) {
    console.log("got GET request");

    /*person1 = {
        name: 'Timmy',
        email: 'timmy@email.com',
        number: '(111) 111-1111'
    };

    person2 = {
        name: 'Emily',
        email: 'emily@email.com',
        number: '(222) 222-2222'
    };

    person3 = {
        name: 'John',
        email: 'john@email.com',
        number: '(333) 333-3333'
    };

    var contactlist = [person1, person2, person3];

    res.json(contactlist)*/

    // console.log(db);

    db.contactlist.find(function (err, docs) {
        console.log(docs);
        res.json(docs);
    });
})

app.post('/contactlist', function (req, res) {

    console.log(req.body);

    db.contactlist.insert(req.body, function (err, doc) {
        res.json(doc);
    });
});

app.delete('/contactlist/:id', function (req, res) {

    var id = req.params.id;
    console.log('removing ' + id);

    db.contactlist.remove({
        _id: mongojs.ObjectId(id),
        function (err, doc) {
            console.log('err' + err);
            res.json(doc);
        }
    }, function (err, doc) {
        res.json(doc);
    });

})

app.listen(3000);

console.log("Server runnning in port 3000");
