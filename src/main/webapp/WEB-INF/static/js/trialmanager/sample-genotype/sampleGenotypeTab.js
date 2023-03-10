(function () {
    'use strict';

    var sampleGenotypeModule = angular.module('sample-genotype-tab', []);

    sampleGenotypeModule.controller('SampleGenotypeCtrl',
        ['$rootScope', '$scope', '$q', '$compile', '$uibModal', 'studyContext', 'DTOptionsBuilder', 'sampleGenotypeService',
            'HasAnyAuthorityService', 'PERMISSIONS',
            function ($rootScope, $scope, $q, $compile, $uibModal, studyContext, DTOptionsBuilder, sampleGenotypeService,
                      HasAnyAuthorityService, PERMISSIONS) {

                $scope.hasAnyAuthority = HasAnyAuthorityService.hasAnyAuthority;
                $scope.PERMISSIONS = PERMISSIONS;

                $scope.nested = {};
                $scope.nested.dtInstance = null;

                const dtOptionsDeferred = $q.defer();
                $scope.dtOptions = dtOptionsDeferred.promise;

                const dtOptions = DTOptionsBuilder.newOptions()
                    .withOption('ajax', function (d, callback) {
                        $.ajax({
                            type: 'POST',
                            url: sampleGenotypeService.getSampleGenotypesTableUrl() + getPageQueryParameters(d),
                            data: JSON.stringify(addFilters({})),
                            success: function (res, status, xhr) {
                                let json = {recordsTotal: 0, recordsFiltered: 0}
                                json.recordsFiltered = xhr.getResponseHeader('X-Filtered-Count');
                                json.recordsTotal = xhr.getResponseHeader('X-Total-Count');
                                json.data = res;
                                setRecordsFiltered(json.recordsFiltered);
                                callback(json);
                            },
                            contentType: 'application/json',
                            beforeSend: function (xhr) {
                                xhr.setRequestHeader('X-Auth-Token', JSON.parse(localStorage['bms.xAuthToken']).token);
                            },
                        });
                    })
                    .withDataProp('data')
                    .withOption('serverSide', true)
                    .withOption('processing', true)
                    .withOption('lengthMenu', [[50, 75, 100], [50, 75, 100]])
                    .withOption('scrollY', '500px')
                    .withOption('scrollCollapse', true)
                    .withOption('scrollX', '100%')
                    .withOption('order', [[2, 'desc']]) //gid
                    .withOption('language', {
                        processing: '<span class="throbber throbber-2x"></span>',
                        lengthMenu: 'Records per page: _MENU_',
                        paginate: {
                            next: '>',
                            previous: '<',
                            first: '<<',
                            last: '>>'
                        }
                    })
                    .withDOM('<"pull-left fbk-left-padding"r>' + //
                        '<"pull-right"B>' + //
                        '<"clearfix">' + //
                        '<"row add-top-padding-small"<"col-sm-12"t>>' + //
                        '<"row"<"col-sm-12 paginate-float-center"<"pull-left"i><"pull-right"l>p>>')
                    .withButtons([{
                        extend: 'colvis',
                        className: 'fbk-buttons-no-border fbk-colvis-button',
                        text: '<i class="glyphicon glyphicon-th"></i>',
                        columns: ':gt(0)'
                    }])
                    .withPaginationType('full_numbers');

                function getPageQueryParameters(data) {
                    var order = data.order && data.order[0];
                    return '?size=' + data.length
                        + '&page=' + ((data.length === 0) ? 0 : data.start / data.length)
                        + '&sort=' + $scope.dtColumns[order.column].name + ',' + order.dir;
                }

                function addFilters(request) {
                    request.studyId = studyContext.studyId;
                    request.filter = {};
                    Object.entries($scope.columns).forEach(([name, column]) => {
                        if (column.filter && column.filter.transform) {
                            column.filter.isFiltered = false;
                            column.filter.transform(request);
                        }
                    });
                    return request;
                }

                $scope.columns = {
                    rowNumber: {
                        data: function () {
                            return "";
                        }
                    },
                    gid: {
                        data: 'gid',
                        filter: {
                            transform(request) {
                                if (this.value) {
                                    request.filter.gidList = this.value.split(',');
                                    this.isFiltered = true;
                                }
                            }
                        }
                    },
                    designation: {
                        data: 'designation',
                        filter: {
                            transform(request) {
                                if (this.value) {
                                    request.filter.designation = this.value;
                                    this.isFiltered = true;
                                }
                            }
                        }
                    },
                    plotNumber: {
                        data: 'plotNumber',
                        filter: {
                            transform(request) {
                                if (this.value) {
                                    request.filter.plotNumberList = this.value.split(',');
                                    this.isFiltered = true;
                                }
                            }
                        }
                    },
                    sampleNumber: {
                        data: 'sampleNo',
                        filter: {
                            transform(request) {
                                if (this.value) {
                                    request.filter.sampleNumberList = this.value.split(',');
                                    this.isFiltered = true;
                                }
                            }
                        }
                    },
                    sampleName: {
                        data: 'sampleName',
                        filter: {
                            transform(request) {
                                if (this.value) {
                                    request.filter.sampleName = this.value;
                                    this.isFiltered = true;
                                }
                            }
                        }
                    }
                };

                $scope.filterHelper = {
                    filterByColumn(filter) {
                        table().ajax.reload();
                    },
                    resetFilterByColumn(filter) {
                        filter.value = null;
                        if (filter.reset) {
                            filter.reset();
                        }
                        table().ajax.reload();
                    },
                    sortColumn(columnName, asc) {
                        table().order([$scope.dtColumns.findIndex((column) => column.name === columnName), asc ? 'asc' : 'desc']).draw();
                    },
                    isSortingAsc(columnName) {
                        const index = $scope.dtColumns.findIndex((column) => column.name === columnName);
                        const order = table().order().find((order) => order[0] === index);
                        if (order) {
                            return order[1] === 'asc';
                        }
                        return null;
                    },
                    getFilteringByClass(filter) {
                        if (filter.isFiltered) {
                            return 'filtering-by';
                        }
                    }
                };

                /**
                 * - column.name used for sorting
                 */
                $scope.dtColumns = Object.entries($scope.columns).map(([name, column]) => {
                    return {
                        name: name,
                        data: column.data,
                        visible: column.visible,
                        orderable: column.orderable
                    }
                });

                $scope.dtColumnDefs = [
                    {
                        // Row Number
                        targets: 0,
                        render: function (data, type, rowData, meta) {
                            return meta.row + 1 + table().page.info().start;
                        }
                    },
                    {
                        targets: "germplasm-link-column",
                        render: function (data, type, rowData, meta) {
                            return '<a class="gid-link" href="javascript: void(0)"'
                                + ` onclick="openGermplasmDetailsPopup('${rowData.gid}')">`
                                + EscapeHTML.escape(data) + '</a>';
                        }
                    },
                    {
                        targets: "_all",
                        orderable: false
                    }

                ];

                $scope.totalItems = 0;


                $scope.getRecordsFiltered = function () {
                    // Replacing this as table().context doesn't contain json property
                    return $scope.recordsFiltered && $scope.recordsFiltered;
                };

                $scope.size = function (obj) {
                    return Object.keys(obj).length;
                };

                function getPageItemIds() {
                    const dataTable = table();
                    if (!dataTable) {
                        return [];
                    }
                    return dataTable.data().toArray().map((data) => {
                        return data.gid;
                    });
                }

                function table() {
                    return $scope.nested.dtInstance.DataTable;
                }

                function setRecordsFiltered(recordsFiltered) {
                    $scope.recordsFiltered = recordsFiltered;
                }

                dtOptionsDeferred.resolve(dtOptions);


            }]);

})();
