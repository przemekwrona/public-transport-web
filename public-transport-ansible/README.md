# public-transport Ansible

Sets up a DigitalOcean Ubuntu/Debian droplet via the `setup` role. Tasks: `certbot` (snap).

Environments live in `inventories/dev` and `inventories/prod` (set the droplet IP in each `hosts.ini`).

    ansible-galaxy collection install community.general
    ansible-playbook -i inventories/dev/hosts.ini site.yml
    ansible-playbook -i inventories/prod/hosts.ini site.yml
    ansible-playbook -i inventories/prod/hosts.ini site.yml --tags certbot


