var App = Ember.Application.create();

App.Router.map(function() {
    this.resource('posts', function() {
        this.route('search');
        this.route('new');
        this.resource('post', { path: ':post_id' }, function() {
            this.route('edit');
            this.route('delete');
        });
    });
    this.resource('sources', function() {
        this.route('new');
        this.resource('source', { path: ':source_id' }, function() {
            this.route('edit');
            this.route('delete');
        });
    });
    this.resource('tags', function() {
        this.route('show');
        this.resource('tag', { path: ':tag_id'});
    });
});

App.ApplicationAdapter = DS.RESTAdapter.extend({
    namespace: 'api/v1'
});

App.ArrayTransform = DS.Transform.extend({
    deserialize: function(deserialized) {
        return deserialized;
    },
    serialize: function(serialized) {
        return serialized;
    }
});
