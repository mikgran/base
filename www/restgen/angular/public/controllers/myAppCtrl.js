var myApp = angular.module('myApp', []);

myApp.controller('AppCtrl', ['$scope', '$http', function ($scope, $http) {

    var resourceName = 'http://localhost:8080/api3/restgen/id/contacts';
    
    var refresh = function () {

        $http.get(resourceName).success(function (response) {

            $scope.contactlist = response;
        });
    }

    refresh();

    $scope.addContact = function () {

        $http.post(resourceName, $scope.contact).success(function (response) {

            refresh();
        });
    };

    $scope.remove = function (id) {

        $http.delete(resourceName + id, $scope.contact).success(function (response) {

            refresh();
        });
    };

}]);