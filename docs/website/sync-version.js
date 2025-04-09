/**
 * Keep the version of the package.json file in sync with the version of the pom.xml file.
 */
import { promises as fs } from 'fs';
import { exec } from 'child_process';
import { promisify } from 'util';

const execAsync = promisify(exec);

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
    const rootPom = await fs.readFile('../../pom.xml', 'utf-8');
    const versionMatch = rootPom.match(/<version>(.*?)<\/version>/);
    if (!versionMatch) {
        console.error('Error: <version> tag not found in pom.xml');
        process.exit(1);
    }
    const rootPomVersion = versionMatch[1];
    const pomVersion = toSemver(rootPomVersion.trim());

    console.log('Pom version resolved:', pomVersion);
    import config from '@exadel/esl/package.json' with {type: 'json'};
    const version = config.version;

    if (version === pomVersion) return;
    console.info(`Updating package.json version to ${pomVersion}`);

    await execAsync(`npm version ${pomVersion} --no-git-tag-version`);
    await execAsync('git add package.json package-lock.json');
})();
