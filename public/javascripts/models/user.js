App.User = DS.Model.extend({
    name: DS.attr('string'),
    lastLoginDate: DS.attr('date')
});