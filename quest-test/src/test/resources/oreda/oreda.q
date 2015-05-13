[QueryGroup="OredaTest"] @collection [[

[QueryItem="ValueDate"]
PREFIX : <http://www.optique-project.eu/resource/Oreda/oreda/item_data/>
select *
{?x :value_date ?y}

[QueryItem="ValueInventory"]
PREFIX : <http://www.optique-project.eu/resource/Oreda/oreda/inv_spec/>
select *
{?x :ref-inventory ?y}

]]

