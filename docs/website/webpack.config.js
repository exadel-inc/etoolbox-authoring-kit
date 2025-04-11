import path, {dirname} from 'path';
import {fileURLToPath} from 'url';

const FILE_ROOT = dirname(fileURLToPath(import.meta.url));

export default {
  mode: 'development',
  devtool: 'source-map',
  entry: {
    'site': './src/site.ts',
    'polyfill-light': './src/polyfill-light.ts'
  },
  resolve: {
    modules: ['../node_modules'],
    roots: [],
    extensions: ['.ts', '.js']
  },
  module: {
    rules: [{
      test: /\.ts?$/,
      loader: 'ts-loader',
      options: {
        compilerOptions: {
          declaration: false
        }
      }
    }]
  },
  output: {
    path: path.resolve(FILE_ROOT, 'dist/bundles'),
    filename: '[name].js'
  }
};
