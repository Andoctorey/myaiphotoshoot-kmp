module.exports = (config, context) => {
  config.devServer = config.devServer || {};
  config.devServer.historyApiFallback = true;
  return config;
};