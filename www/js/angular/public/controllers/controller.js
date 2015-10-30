var myApp = angular.module('myApp', []);

myApp.controller('AppCtrl', ['$scope', '$http', function ($scope, $http) {

    console.log("Hello World from controller");

    var refresh = function () {

        $http.get('/contactlist').success(function (response) {
            console.log("Got data I requested");

            $scope.contactlist = response;
        });
    }

    refresh();

    $scope.addContact = function () {
        console.log($scope.contact);
        $http.post('/contactlist', $scope.contact).success(function (response) {
            console.log(response);
            refresh();
        })

    };

    $scope.remove = function (id) {
        console.log('removing ' + id);

        $http.delete('/contactlist/' + id)
            .success(function (response) {
                refresh();
            });
    };


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
    $scope.contactlist = contactlist;*/

}]);
