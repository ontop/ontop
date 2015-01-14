[QueryGroup="DateTimeStampOrders"] @collection [[

[QueryItem="RequiredDate"]
PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Orders/>
select *
{?x :RequiredDate ?y}

[QueryItem="ShippedDate"]
PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Orders/>
select *
 {?x :ShippedDate ?y}
]]

[QueryGroup="DateTimeStampEmployees"] @collection [[

[QueryItem="HireDate"]
PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Employees/>
select *
{?x :HireDate ?y}

[QueryItem="BirthDate"]
PREFIX : <http://www.optique-project.eu/resource/northwind/northwind/Employees/>
select *
{?x :BirthDate ?y}

]]