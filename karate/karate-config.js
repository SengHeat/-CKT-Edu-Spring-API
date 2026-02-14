function fn() {
  var env = karate.env;
  var config = {
    baseUrl: 'http://localhost:8080',
    apiKey: ''
  };

  if (env == null || env == '') {
    env = 'dev';
  }

  karate.log('Karate Environment:', env);

  if (env == 'dev') {
    config.baseUrl = 'http://localhost:8080';
  } else if (env == 'test') {
    config.baseUrl = 'http://localhost:8081';
  } else if (env == 'prod') {
    config.baseUrl = 'https://api.production.com';
  }

  karate.configure('connectTimeout', 10000);
  karate.configure('readTimeout', 10000);

  return config;
}
