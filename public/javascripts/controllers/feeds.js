App.OptionsView = Em.CollectionView.extend({
    attributeBindings: ['labelPath'],
    tagName: 'div',
    content: ["1", "2"],
    templateName: 'option-group',
    labelPath: 'content',
    valuePath: 'content',
    itemViewClass: Em.View.extend({
        templateName: 'option-item',
        selectedBinding: 'parentView.selected',
        init: function() {
            this.labelPathDidChange();
            this.valuePathDidChange();
            this._super();
        },
        labelPathDidChange: Ember.observer(function() {
            var labelPath = this.get('parentView.labelPath');
            if (!labelPath) { return; }
            Ember.defineProperty(this, 'label', Ember.computed(function() {
                return this.get(labelPath);
            }).property(labelPath));
        }, 'parentView.labelPath'),
        valuePathDidChange: Ember.observer(function() {
            var valuePath = this.get('parentView.valuePath');
            if (!valuePath) { return; }
            Ember.defineProperty(this, 'value', Ember.computed(function() {
                return this.get(valuePath);
            }).property(valuePath));
        }, 'parentView.valuePath')
    })
});

App.OptionCheckbox = Em.Checkbox.extend({
    valueBinding: 'parentView.value',
    selectedBinding: 'parentView.selected',
    attributeBindings: ['value', 'checked'],

    checked: function () {
        var selected = this.get('selected');
        return selected.contains(this.get('value'));
    }.property('value', 'selected.@each'),

    click: function (evt) {
        var isPresent = this.get('checked'),
            selected  = this.get('selected'),
            value      = this.get('value');
        if (isPresent)
            selected.pushObject(value);
        else
            selected.removeObject(value);
    }
});


App.FeedsRoute = Em.Route.extend({
    authRedirectable: true,
    model: function() {
        return this.store.find('feed');
    }
});


App.FeedsNewRoute = Em.Route.extend({
    authRedirectable: true,
    setupController: function(controller) {
        controller.set('userSources', this.store.find('source'));
    },
    actions: {
        create: function() {
            var self = this;
            console.log(this.controller.get('selectedSources'));
            var feed = this.store.createRecord('feed', this.controller.getProperties(['name', 'description']));
            var sources = this.controller.get('sources');
            feed.save().then(function(feed) {
                feed.get('sources').addObjects(sources);
                feed.save().then(function(feed) {
                    self.transitionTo('feed', feed);
                });
            });
        }
    }
});

App.FeedsNewController = Em.Controller.extend({
    userSources: null,
    sources: []
});

App.FeedRoute = Em.Route.extend({
    model: function(params) {
        return this.store.find('feed', params.feed_id);
    }
});

App.FeedEditRoute = Em.Route.extend({
    authRedirectable: true,
    setupController: function(controller) {
        controller.set('userSources', this.store.find('source'));
    },
    actions: {
        update: function() {
            var self = this;
            var feed = this.modelFor('feed');
            feed.setProperties(this.controller.getProperties(['name', 'description']));
            var sources = this.controller.get('sources');
            feed.get('sources').clear().addObjects(sources);
            feed.save().then(function(feed) {
                self.transitionTo('feed', feed);
            });
        }
    }
});

App.FeedEditController = Em.Controller.extend({
    needs: ['feed'],
    userSources: null,
    sources: [],
    nameBinding: Em.Binding.oneWay('controllers.feed.name'),
    descriptionBinding: Em.Binding.oneWay('controllers.feed.description'),
    sources: function() {
        var sources = this.get('controllers.feed.sources');
        return sources.slice(0);
    }.property('controllers.feed.sources.@each')
});

App.FeedDeleteRoute = Ember.Route.extend({
    authRedirectable: true,
    actions: {
        delete: function() {
            var self = this;
            var feed = this.modelFor('feed');
            feed.deleteRecord();
            feed.save().then(function() {
                self.transitionTo('feeds');
            });
        },
        cancel: function() {
            this.transitionTo('feeds');
        }
    }
});