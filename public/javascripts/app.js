var App = Ember.Application.create();

App.Router.map(function() {
    this.resource('posts', function() {
        this.route('new');
        this.resource('post', { path: ':post_id' }, function() {
            this.route('edit');
            this.route('delete');
        });
    });
});

App.ApplicationAdapter = DS.RESTAdapter.extend({
    namespace: 'api/v1'
});