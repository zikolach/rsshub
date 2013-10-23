App.Comment = DS.Model.extend({
    comment:    DS.attr('string'),
    updateDate: DS.attr('isoDate'),
    post:       DS.belongsTo('post'),
    userName:   DS.attr('string')
});