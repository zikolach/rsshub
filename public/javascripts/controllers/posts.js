App.PostsRoute = Ember.Route.extend({
   model: function() {
        return this.store.find("post");
   }
});

App.PostsNewRoute = Ember.Route.extend({
    actions: {
        create: function() {
            var self = this;
            var post = this.store.createRecord('post', this.controller.getProperties(['name', 'text']));
            post.save().then(function(post) {
                self.transitionTo('post', post);
            });
        }
    }
});

App.PostEditRoute = Ember.Route.extend({
    setupController: function(controller) {
        controller.setProperties((this.modelFor('post').getProperties(['name', 'text'])));
    },
    actions: {
        update: function() {
            var self = this;
            post = this.modelFor('post')
            post.setProperties(this.controller.getProperties(['name', 'text']));
            post.save().then(function(post) {
                self.transitionTo('post', post);
            });
        }
    }
});

App.PostDeleteRoute = Ember.Route.extend({
    actions: {
        delete: function() {
            var self = this;
            post = this.modelFor('post');
            post.deleteRecord();
            post.save().then(function() {
                self.transitionTo('posts');
            });
        }
    }
});