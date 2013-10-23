App.PostsRoute = Ember.Route.extend({
    authRedirectable: true,
//     redirect: function() {
//        console.log(this);
//         this.transitionTo("posts.search");
//     }
 });

App.PostsIndexRoute = Ember.Route.extend({
    authRedirectable: true,
//    redirect: function() {
//        this.transitionTo("posts.search");
//    }
});

App.PostsSearchRoute = Ember.Route.extend({
    authRedirectable: true,
    actions: {
        search: function(criteria) {
            var self = this;
            this.store.findQuery("post", { search: criteria }).then(function(posts) {
                self.controller.set('model', posts);
            });
        }
    }

});

App.PostsSearchController = Ember.ArrayController.extend({
    sortProperties: ['distance','title'],
    sortAscending: true
});

App.PostsMyRoute = Ember.Route.extend({
    authRedirectable: true,
    model: function(criteria) {
        var self = this;
        return this.store.findQuery("post", { owner: 'me' });
    }

});

App.PostsMyController = Ember.ArrayController.extend({
    sortProperties: ['title'],
    sortAscending: true
});


App.PostsNewRoute = Ember.Route.extend({
    authRedirectable: true,
    actions: {
        create: function() {
            var self = this;
            var params = this.controller.getProperties(['title', 'link', 'description']);
            var pubDate = moment(this.controller.get('pubDate'));
            if (!pubDate.isValid())
                this.controllerFor('application').alert('Invalid date');
            else if (!params['title'] || !params['link'] || !params['description'])
                this.controllerFor('application').alert('Empty fields');
            else {
                var post = this.store.createRecord('post', params);
                post.set('pubDate', moment(pubDate).unix()*1000 + moment(pubDate).milliseconds());
                post.save().then(function(post) {
                    self.transitionTo('post', post);
                }, function(err) {
                    console.log(err);
                });
            }
        }
    }
});

App.PostEditRoute = Ember.Route.extend({
    authRedirectable: true,
    setupController: function(controller) {
        var post = this.modelFor('post')
        controller.setProperties(post.getProperties(['title', 'link', 'description']));
        controller.set('pubDate', moment(post.get('pubDate')).format('YYYY.MM.DD hh:mm:ss'));
    },
    actions: {
        update: function() {
            var self = this;
            var pubDate = moment(this.controller.get('pubDate'));
            if (!pubDate.isValid())
                this.controllerFor('application').alert('Invalid date');
            else {
                post = this.modelFor('post')
                post.setProperties(this.controller.getProperties(['title', 'link', 'description']));
                post.set('pubDate', moment(pubDate).unix()*1000 + moment(pubDate).milliseconds());
                post.save().then(function(post) {
                    self.transitionTo('post', post);
                });
            }

        },
        cancel: function() {
            var self = this;
            post = this.modelFor('post');
            self.transitionTo('post', post);
        }
    }
});

App.PostDeleteRoute = Ember.Route.extend({
    authRedirectable: true,
    actions: {
        delete: function() {
            var self = this;
            var post = this.modelFor('post');
            var userId = this.auth.get('userId');
            if (userId === post.get('userId')) {
                post.deleteRecord();
                post.save().then(function() {
                    self.transitionTo('posts');
                });
            } else {
                self.controllerFor('application').alert('You can not delete this post');
            }
        },
        cancel: function() {
            var self = this;
            post = this.modelFor('post');
            self.transitionTo('post', post);
        }
    }
});