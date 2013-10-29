App.SourcesRoute = Ember.Route.extend({
   authRedirectable: true,
   model: function() {
        return this.store.find("source");
   }
});

App.SourcesNewRoute = Ember.Route.extend({
    authRedirectable: true,
    setupController: function(controller) {
        controller.set('name', '');
        controller.set('url', '');
    },
    actions: {
        create: function() {
            var self = this;
            var source = this.store.createRecord('source', this.controller.getProperties(['name', 'url']));
            source.save().then(function(source) {
                self.transitionTo('source', source);
            }, function(err) {
                console.log(err);
                self.controllerFor('application').alert("Create source failed");
                source.deleteRecord();
            });
        }
    }
});

App.SourceEditRoute = Ember.Route.extend({
    authRedirectable: true,
    setupController: function(controller) {
        controller.setProperties((this.modelFor('source').getProperties(['name', 'url'])));
    },
    actions: {
        update: function() {
            var self = this;
            var source = this.modelFor('source')
            source.setProperties(this.controller.getProperties(['name', 'url']));
            source.save().then(function(source) {
                self.transitionTo('source', source);
            });
        }
    }
});

App.SourceDeleteRoute = Ember.Route.extend({
    authRedirectable: true,
    actions: {
        delete: function() {
            var self = this;
            var source = this.modelFor('source');
            source.deleteRecord();
            source.save().then(function(source) {
                self.transitionTo('sources');
            });
        },
        cancel: function() {
            var self = this;
            var source = this.modelFor('source');
            self.transitionTo('source', source);
        }
    }
});