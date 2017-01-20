var myApp = angular.module('myApp', []);

myApp.controller('AppCtrl', ['$scope', '$http', function ($scope, $http) {

    var refresh = function () {

        $http.get('http://localhost:8080/api2/contacts').success(function (response) {

            $scope.contactlist = response;
        });
    }

    refresh();


    $scope.addContact = function () {

        $http.post('http://localhost:8080/api2/contacts', $scope.contact).success(function (response) {

            refresh();
        });
    };

    $scope.remove = function (id) {

        $http.delete('http://localhost:8080/api2/contacts/' + id, $scope.contact).success(function (response) {

            refresh();
        });
    };

}]);