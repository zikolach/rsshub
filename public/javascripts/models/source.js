App.Source = DS.Model.extend({
    name: DS.attr('string'),
    url: DS.attr('string'),
    fetchDate: DS.attr('date')
});