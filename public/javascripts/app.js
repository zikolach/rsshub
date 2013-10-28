var App = Ember.Application.create({
    LOG_TRANSITIONS: true,
});

App.Router.map(function() {
    this.resource('posts', function() {
        this.route('search');
        this.route('my');
        this.route('new');
        this.resource('post', { path: ':post_id' }, function() {
            this.route('edit');
            this.route('delete');
            this.resource('comments', function() {
                this.route('new');
                this.route('comment', { path: ':comment_id' });
            });
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
    this.resource('feeds', function() {
        this.route('new');
        this.resource('feed', { path: ':feed_id' }, function() {
            this.route('edit');
            this.route('delete');
        });
    });
    this.resource('profile', function() {
        this.route('show');
        this.route('edit');
    });
    this.route('login');
    this.route('register');
    this.route('logout');
});

App.ApplicationRoute = Ember.Route.extend({});

App.ApplicationController = Ember.Controller.extend({
    alert: function(message) {
        this.set('message', message);
    },
    messageObserver: function() {
        var message = this.get('message');
        if (message)
            if (message.match(/success/gi))
                this.set('messageClass', 'alert-success');
            else
                this.set('messageClass', 'alert-warning');
        else
            this.set('messageClass', null);
    }.observes('message'),
    actions: {
        close: function() {
            this.set('message', null);
        }
    }
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

App.IsoDateTransform = DS.Transform.extend({
    deserialize: function(deserialized) {
        return deserialized;
    },
    serialize: function(serialized) {
//        var m = moment(serialized)
//        return m.unix()*1000 + m.milliseconds();
        return serialized;
    }
});


App.IndexRoute = Ember.Route.extend({
});

Ember.Handlebars.helper('from-now', function(value) {
  if (value) return moment(value).fromNow();
  return 'never';
});

$.postJSON = function(url, data, callback) {
    return jQuery.ajax({
        'type': 'POST',
        'url': url,
        'contentType': 'application/json',
        'data': JSON.stringify(data),
        'dataType': 'json',
        'success': callback
    });
};


App.Auth = Em.Auth.extend({
    request: 'jquery',
    response: 'json',
    strategy: 'token',
    tokenKey: 'token',
    tokenIdKey: 'userId',
    tokenLocation: 'customHeader',
    tokenHeaderKey: 'token',
    session: 'cookie',
    signInEndPoint: '/api/v1/login',
    signOutEndPoint: '/api/v1/logout',
    modules: ['emberData', 'authRedirectable', 'rememberable', 'actionRedirectable'],
    emberData: {
        userModel: 'user'
    },
    authRedirectable: {
        route: 'login'
    },
    rememberable: {
        tokenKey: 'token',
        period: 365,
        autoRecall: true
    },
    actionRedirectable: {
        signInRoute: 'profile',
        signInSmart: true,
        signInBlacklist: [
          'register',
          'login'
        ],
        signOutRoute: 'index'
    }
});