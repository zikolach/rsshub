App.PostsRoute = Ember.Route.extend({
    authRedirectable: true,
 });

App.PostsIndexRoute = Ember.Route.extend({
    authRedirectable: true,
});

App.PostsSearchRoute = Ember.Route.extend({
    authRedirectable: true,
    actions: {
        search: function(criteria) {
            var self = this;
            this.store.findQuery('post', {
                search: criteria,
                start: 0,
                count: 10
            }).then(function(posts) {
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
        return this.store.findQuery('post', {
            owner: 'me',
            start: 0,
            count: 10,
            sort: ['title']
        });
    },
    setupController: function(controller, model) {
        controller.set('model', model);
        controller.set('fetchable', model.get('length') > 0);
    },
    actions: {
        fetchNext: function() {
            var self = this;
            var model = this.modelFor('posts.my')
            var start = model.get('length');
            var sort = this.controller.get('sortProperties');
            console.log(sort);
            this.store.findQuery('post', {
                owner: 'me',
                start: start,
                count: 10,
                sort: sort
            }).then(function(posts) {
                if (posts.get('length') > 0)
                    model.addObjects(posts);
                else {
                    self.controller.set('fetchable', false);
                }
            });
        }
    }
});

App.PostsMyController = Ember.ArrayController.extend({
    sortProperties: ['title'],
    sortAscending: true,
    fetchable: true
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