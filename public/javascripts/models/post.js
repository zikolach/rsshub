App.ArrayTransform = DS.Transform.extend({
    deserialize: function(deserialized) {
        return deserialized;
    },
    serialize: function(serialized) {
        return serialized;
    }
});

App.Post = DS.Model.extend({
    name: DS.attr('string'),
    text: DS.attr('string'),
    distance: DS.attr('number'),
    tags: DS.attr('array')
})