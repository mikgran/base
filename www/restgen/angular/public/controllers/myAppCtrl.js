var myApp = angular.module('myApp', []);

myApp.controller('AppCtrl', ['$scope', '$http', function ($scope, $http) {

    // TOIMPROVE: change the restgen/id/<classname> --> strip the .../id/... part?
    var resourceName = 'http://localhost:8080/api3/restgen/id/contacts/';

    var refresh = function () {

        $http.get(resourceName).then(function(success) {

            console.log(success);

            $scope.contactlist = success.data;

        }, function(error) {
            console.log(error);
        });
    }

    refresh();

    $scope.addContact = function () {

        $http.post(resourceName, $scope.contact).then(function(success) {

             refresh();

        }, function(error) {
            console.log(error);
        });

    };

    $scope.remove = function (id) {

        $http.delete(resourceName + id, $scope.contact).then(function(success) {

             refresh();

        }, function(error) {
            console.log(error);
        });
    };

}]);