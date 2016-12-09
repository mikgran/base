var myApp = angular.module('myApp', []);

myApp.controller('AppCtrl', ['$scope', '$http', function ($scope, $http) {

    console.log("Hello World from controller")

    var refresh = function () {

        /*$http.get('/contactlist').success(function (response) {
            console.log("Got data I requested");

            $scope.contactlist = response;
        });*/

        // instead of using mongodb, use java REST
        $http.get('http://localhost:8080/api2/contactlist').success(function (response) {

            console.log("Got data I requested");
            console.log(JSON.stringify(response, null, 2));

            $scope.contactlist = response;
        });

    }

    refresh();

    $scope.addContact = function () {

        console.log($scope.contact);

        $http.post('http://localhost:8080/api2/contactlist', $scope.contact).success(function (response) {

            console.log(response);
            refresh();
        });

    };

    $scope.remove = function (id) {

        console.log('removing ' + id);

        $http.delete('/contactlist/' + id)
            .success(function (response) {

                refresh();
            });
    };

}]);