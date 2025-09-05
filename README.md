# Cloudflare ONI User Coordinate Like Service

A **Cloudflare Worker** that manages liked coordinates of logged-in Steam users using **Cloudflare D1**.

Built with Kotlin/JS.

This service is part of my [ONI Seed Browser](https://stefan-oltmann.de/oni-seed-browser).

## How it works

1. A client authenticates with a **token** containing a Steam ID.
2. The Worker supports three operations:
    - **PUT** → add a like for a coordinate.
    - **DELETE** → remove a like for a coordinate.
    - **GET** → return all liked coordinates for the user.

## Run & Deploy

```
wrangler dev
```

```
wrangler deploy
```

## License

This Cloudflare worker is licensed under the GNU Affero General Public License (AGPL),
ensuring the community's freedom to use, modify, and distribute the software.

In short, you can’t make a closed-source copy of the entire project,
but you're welcome to study it and reuse parts in your own projects.
