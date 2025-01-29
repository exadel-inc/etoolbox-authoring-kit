/**
 * Keep the version of the package.json file in sync with the version of the pom.xml file.
 */
const { promisify } = require('util');
const fs = require('fs').promises;
const exec = promisify(require('child_process').exec);

const toSemver = (version) => {
    const [major, minor, patch, post] = version.split('.');
    if (post) {
        const [pre, build] = post.split('-');
        return `${major}.${minor}.${patch}-${build || 'RELEASE'}.${pre}`;
    }
    return `${major}.${minor}.${patch}`;
};

(async() => {
    console.log('Checking pom.xml version...');
    const rootPom = await fs.readFile('../pom.xml', 'utf-8');
    const rootPomVersion = rootPom.match(/<version>(.*?)<\/version>/)[1];
    const pomVersion = toSemver(rootPomVersion.trim());

    console.log('Pom version resolved:', pomVersion);
    const { version } = require('./package.json');

    if (version === pomVersion) return;
    console.info(`Updating package.json version to ${pomVersion}`);

    await exec(`npm version ${pomVersion} --no-git-tag-version`);
    await exec('git add package.json package-lock.json');
})();
