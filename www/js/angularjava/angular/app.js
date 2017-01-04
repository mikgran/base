var express = require('express');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');

var app = express();

app.use(logger('dev'));

app.use(express.static(__dirname + "/public"));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
    extended: false
}));
app.use(cookieParser());

/*app.get('/contactlist', function (req, res) {
    console.log("got GET request");

    person1 = {
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

    res.json(contactlist);

    // console.log(db);

    / *db.contactlist.find(function (err, docs) {
        console.log(docs);
        res.json(docs);
    });* /
})*/

/*app.post('/contactlist', function (req, res) {

    console.log("got POST request");
    console.log(req.body);

    / *db.contactlist.insert(req.body, function (err, doc) {
        res.json(doc);
    });* /
});*/

app.delete('/contactlist/:id', function (req, res) {

    var id = req.params.id;

    console.log("got DELETE request: id: " + id);

    /*db.contactlist.remove({
        _id: mongojs.ObjectId(id),
        function (err, doc) {
            console.log('err' + err);
            res.json(doc);
        }
    }, function (err, doc) {
        res.json(doc);
    });*/

})

module.exports = app;
