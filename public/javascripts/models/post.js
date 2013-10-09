App.Post = DS.Model.extend({
    name: DS.attr('string'),
    text: DS.attr('string'),
    distance: DS.attr('number')
})